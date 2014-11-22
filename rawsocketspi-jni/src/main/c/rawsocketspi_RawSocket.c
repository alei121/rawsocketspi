/*
 * rawsocketspi_RawSocket.c
 *
 * This RawSocket opens socket with socket(PF_PACKET, SOCK_RAW, htons(ETH_P_ALL))
 *
 * gcc -shared -I/usr/lib/jvm/java-7-openjdk-amd64/include -I/usr/lib/jvm/java-7-openjdk-amd64/include/linux -o libRawSocket.so code_messy_net_RawSocket.c
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/ioctl.h>
#include <netinet/ip.h>
#include <signal.h>
#include <fcntl.h>
#include <sys/time.h>
#define __FAVOR_BSD
#include <netinet/tcp.h>
#include <netinet/if_ether.h>
#include <linux/if_packet.h>
#include <linux/if.h>
#include <linux/filter.h>

// For ARPHDR definitions
#include <net/if_arp.h>

// For getifaddrs
#include <ifaddrs.h>

// epoll
#include <sys/epoll.h>

// #include <net/bpf.h>
// trying pcap.h this for bpf constants
// #include <pcap.h>

#include "rawsocketspi_RawSocket.h"

JNIEXPORT void JNICALL Java_rawsocketspi_RawSocket_nativeInit
  (JNIEnv *env, jclass cls)
{
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeOpen
  (JNIEnv *env, jclass cls, jstring intf)
{
	const char *ifname;
    struct sock_fprog filter;
    struct ifreq ethreq;
    int ifindex;
    struct sockaddr_ll sll;
    int sockfd;

   // Capture 1600 byte ethernet frame and filter for TCP and UDP packets.
   struct sock_filter bpf_code[] =
   {
       // ip ethertype? set 14 as offset
       BPF_STMT(BPF_LD+BPF_H+BPF_ABS, 12),
       BPF_JUMP(BPF_JMP+BPF_JEQ+BPF_K, 0x0800, 0, 1),
       BPF_STMT(BPF_LDX+BPF_W+BPF_IMM, 14),

       // vlan ethertype? set 18 as offset
       BPF_JUMP(BPF_JMP+BPF_JEQ+BPF_K, 0x8100, 0, 5),
       BPF_STMT(BPF_LDX+BPF_W+BPF_IMM, 18),

       // tcp/udp? get it
       BPF_STMT(BPF_LD+BPF_B+BPF_IND, 9),
       BPF_JUMP(BPF_JMP+BPF_JEQ+BPF_K, 0x06, 1, 0),
       BPF_JUMP(BPF_JMP+BPF_JEQ+BPF_K, 0x11, 0, 1),
       BPF_STMT(BPF_RET+BPF_K, 1600),
       BPF_STMT(BPF_RET+BPF_K, 0)
   };

	sockfd = socket(PF_PACKET, SOCK_RAW, htons(ETH_P_ALL));
	if (sockfd < 0) {
		perror("nativeOpen SOCK_RAW");
		return -1;
	}

	// get ifindex
	memset(&ethreq, 0, sizeof(ethreq));
	ifname = (*env)->GetStringUTFChars(env, intf, NULL);
    if (ifname == NULL) {
		fprintf(stderr, "Unable to GetStringUTFChars\n");
    	return -1;
    }
	strcpy(ethreq.ifr_name, ifname);
    (*env)->ReleaseStringUTFChars(env, intf, ifname);
	if (ioctl(sockfd, SIOCGIFINDEX, &ethreq) == -1) {
		perror("nativeOpen SIOCGIFINDEX");
		return -1;
	}
	ifindex = ethreq.ifr_ifindex;

	// get flags
	if (ioctl(sockfd, SIOCGIFFLAGS, &ethreq) == -1) {
		perror("nativeOpen SIOCGIFFLAGS");
		return -1;
	}

	// Setting promiscuous mode
	ethreq.ifr_flags |= IFF_PROMISC;
	if (ioctl(sockfd, SIOCSIFFLAGS, &ethreq) == -1) {
		perror("nativeOpen SIOCSIFFLAGS");
		return -1;
	}

	// Bring up network interface
	ethreq.ifr_flags |= IFF_UP;
	if (ioctl(sockfd, SIOCSIFFLAGS, &ethreq) == -1) {
		perror("nativeOpen SIOCSIFFLAGS");
		return -1;
	}

	// Bind sock_descr socket to network interface
	memset(&sll, 0, sizeof(sll));
	sll.sll_family = AF_PACKET;
	sll.sll_protocol = htons(ETH_P_ALL);
	sll.sll_ifindex = ifindex;
	if (bind(sockfd, (struct sockaddr *)&sll, sizeof sll) == -1) {
		perror("nativeOpen bind");
		return -1;
	}

	/*
	// Attach the filter to the socket
	filter.len = sizeof(bpf_code)/sizeof(bpf_code[0]);
	filter.filter = bpf_code;
	if (setsockopt(sockfd, SOL_SOCKET, SO_ATTACH_FILTER, &filter, sizeof(filter)) < 0) {
		fprintf(stderr, "Attach filter error\n");
		return -1;
	}
	*/

	/*
	 *
	 * To block outgoing packets
	unsigned int enabled = 0;
	if (ioctl(sockfd, BIOCSSEESENT, &enabled) == -1) {
		fprintf(stderr, "Failed to disable seeing on outgoing packets\n");
		return -1;
	}
	*/

	return sockfd;
}


JNIEXPORT void JNICALL Java_rawsocketspi_RawSocket_nativeClose
  (JNIEnv *env, jclass cls, jint sd)
{
	struct ifreq ethreq;
    struct sockaddr_ll sll;
    socklen_t len = sizeof(struct sockaddr_ll);

	memset(&ethreq, 0, sizeof(ethreq));
	memset(&sll, 0, sizeof(sll));

	if (getsockname(sd, (struct sockaddr *)&sll, &len) == -1) {
		perror("natvieClose getsockname");
		return;
	}

	ethreq.ifr_ifindex = sll.sll_ifindex;
	if (ioctl(sd, SIOCGIFNAME, &ethreq) == -1) {
		perror("natvieClose SIOCGIFNAME");
		return;
	}

	if (ioctl(sd, SIOCGIFFLAGS, &ethreq) == -1) {
		perror("nativeClose SIOCGIFFLAGS");
		return;
	}
	ethreq.ifr_flags &= ~IFF_PROMISC;
	ethreq.ifr_flags &= ~IFF_NOARP;
	ethreq.ifr_flags |= IFF_UP;
	if (ioctl(sd, SIOCSIFFLAGS, &ethreq) == -1) {
		perror("nativeClose SIOCSIFFLAGS");
		return;
	}
	close(sd);
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeRead
  (JNIEnv *env, jclass cls, jint sd, jobject bb, jint offset, jint length)
{
	char *buffer = (*env)->GetDirectBufferAddress(env, bb);
//	int capacity = (*env)->GetDirectBufferCapacity(env, bb);
	int len = recv(sd, buffer + offset, length, 0);
	if (len == -1) {
		perror("recv");
	}
	return len;
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeWrite
  (JNIEnv *env, jclass cls, jint sd, jobject bb, jint offset, jint length)
{
	char *buffer = (*env)->GetDirectBufferAddress(env, bb);
//	int capacity = (*env)->GetDirectBufferCapacity(env, bb);
	return send(sd, buffer + offset, length, 0);
}

JNIEXPORT jobjectArray JNICALL Java_rawsocketspi_RawSocket_nativeGetInterfaceNames
  (JNIEnv *env, jclass obj)
{
    jobjectArray jb;
    int i;
    int count;

	struct ifaddrs *ifaddr, *ifa;

	if (getifaddrs(&ifaddr) == -1) {
		perror("getifaddrs");
		return NULL;
	}

	// get count
	count = 0;
	for (ifa = ifaddr; ifa != NULL; ifa = ifa->ifa_next) {
		if (ifa->ifa_addr->sa_family == PF_PACKET) {
			count++;
		}
	}

	// create array
    jb = (*env)->NewObjectArray(env, count, (*env)->FindClass(env, "java/lang/String"), NULL);

    // populate array
    i = 0;
	for (ifa = ifaddr; ifa != NULL; ifa = ifa->ifa_next) {
		if (ifa->ifa_addr->sa_family == PF_PACKET) {
			(*env)->SetObjectArrayElement(env, jb, i, (*env)->NewStringUTF(env, ifa->ifa_name));
			i++;
		}
	}

	freeifaddrs(ifaddr);

    return jb;
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeGetHardwareAddress
  (JNIEnv *env, jclass obj, jint sd, jobject bb)
{
	char *buffer = (*env)->GetDirectBufferAddress(env, bb);
	int capacity = (*env)->GetDirectBufferCapacity(env, bb);

    struct sockaddr_ll sll;
    int i;
    socklen_t len = sizeof(struct sockaddr_ll);

	memset(&sll, 0, sizeof(sll));

	// get interface info
	if (getsockname(sd, (struct sockaddr *)&sll, &len) == -1) {
		perror("getHardwareAddress getsockname");
		return -1;
	}

	if (capacity < sll.sll_halen) {
		fprintf(stderr, "Buffer overflow for getHardwareAddress: capacity=%d, halen=%d\n", capacity, sll.sll_halen);
		return -1;
	}

	for (i = 0; i < sll.sll_halen; i++) {
		buffer[i] = sll.sll_addr[i];
	}

	return sll.sll_halen;
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeEpollCreate
  (JNIEnv *env, jclass obj, jint size)
{
	return epoll_create(size);
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeEpollCreate1
  (JNIEnv *env, jclass obj, jint flag)
{
	return epoll_create1(flag);
}

static int _java_epollop_to_c(jint op)
{
	// Must match constants in Java
	switch (op) {
	case 1: return EPOLL_CTL_ADD;
	case 2: return EPOLL_CTL_MOD;
	case 3: return EPOLL_CTL_DEL;
	}
	return -1;
}

static void _epoll_event_java_to_c(JNIEnv *env, jobject jevent, struct epoll_event *event)
{
	  jclass cls = (*env)->FindClass(env, "rawsocketspi/RawSocket$EpollEvent");
	  jfieldID eventsID = (*env)->GetFieldID(env, cls, "events", "I");
	  jfieldID dataID = (*env)->GetFieldID(env, cls, "data", "J");

	  // Events. Must match constants in Java
	  jint events = (*env)->GetIntField(env, jevent, eventsID);
	  if (events & 1) event->events |= EPOLLIN;
	  if (events & 2) event->events |= EPOLLOUT;
	  if (events & 4) event->events |= EPOLLET;

	  // Data
	  event->data.u64 = (*env)->GetIntField(env, jevent, dataID);
}

static void _epoll_event_c_to_java(JNIEnv *env, struct epoll_event *event, jobject jevent)
{
	  jclass cls = (*env)->FindClass(env, "rawsocketspi/RawSocket$EpollEvent");
	  jfieldID eventsID = (*env)->GetFieldID(env, cls, "events", "I");
	  jfieldID dataID = (*env)->GetFieldID(env, cls, "data", "J");

	  // Events. Must match constants in Java
	  int events  = 0;
	  if (event->events & EPOLLIN) events |= 1;
	  if (event->events & EPOLLOUT) events |= 2;
	  if (event->events & EPOLLET) events |= 4;
	  (*env)->SetIntField(env, jevent, eventsID, events);

	  (*env)->SetIntField(env, jevent, dataID, event->data.u64);
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeEpollCtl
  (JNIEnv *env, jclass obj, jint epfd, jint op, jint fd, jobject jevent)
{
	struct epoll_event event;
	_epoll_event_java_to_c(env, jevent, &event);
	return epoll_ctl(epfd, _java_epollop_to_c(op), fd, &event);
}

JNIEXPORT jint JNICALL Java_rawsocketspi_RawSocket_nativeEpollWait
  (JNIEnv *env, jclass obj, jint epfd, jobjectArray jevents, jint maxevents, jint timeout)
{
	int i;
	struct epoll_event events[maxevents];
	jsize jevents_count = (*env)->GetArrayLength(env, jevents);
	if (jevents_count < maxevents) return -1;

	int count = epoll_wait(epfd, events, maxevents, timeout);
	if (count == -1) return -1;

	for (i = 0; i < count; i++) {
		jobject jevent = (*env)->GetObjectArrayElement(env, jevents, i);
		_epoll_event_c_to_java(env, &(events[i]), jevent);
	}

	return count;
}
