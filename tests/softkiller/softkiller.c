/* X Window app killer
 *
 * Author: Pavel Tisnovsky <ptisnovs@redhat.com>
 *
 * Compile:
 *     gcc -Wall -pedantic -std=c99 -o softkiller softkiller.c -lX11
 *     (please note that -std=c99 is needed because we use snprintf
 *      function which does not exist in C89/ANSI C)
 *
 * Run:
 *     ./softkiller PID
 */



#include <stdio.h>
#include <stdlib.h>
#include <glob.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <X11/Xlib.h>
#include <X11/Xatom.h>

/*
 * Number of long decimal digits + 1 (for ASCIIZ storage)
 */
#define MAX_LONG_DECIMAL_DIGITS 21

/*
 * Max line length in /proc/stat files
 */
#define MAX_LINE 8192

/*
 * Max filename length for /proc/... files
 */
#define MAX_FILENAME 32

/*
 * Return values
 */
#define EXIT_CODE_OK 0
#define EXIT_CODE_ERROR 1

/*
 * Different softkilling strategies
 */
#define TRY_TO_CLOSE_WINDOW 1
#define TRY_TO_KILL_WINDOW 1

/*
 * Delay between application of different softkilling strategies.
 */
#define SLEEP_AMOUNT 2

/*
 * Not in c89/c99...
 */
#define file_no(FP) ((FP)->_fileno)

/*
 * Basic information about given process
 */
typedef struct ProcStruct
{
    long uid, pid, ppid;
    char cmd[MAX_LINE];
} ProcStruct;

ProcStruct *P = NULL;

int N = 0;

Display *display;
Window root_window;
Atom atom_pid;



/*
 * Read basic process info from the file /proc/${PID}/stat
 * where ${PID} is process ID.
 */
int read_process_info(char *file_name_part, ProcStruct *P)
{
    FILE *fin;
    char filename[MAX_FILENAME];
    struct stat stat;

    /* try to open file /proc/${PID}/stat for reading */
    snprintf(filename, sizeof(filename), "%s%s", file_name_part, "/stat");
    fin = fopen(filename, "r");

    if (fin == NULL)
    {
        return 0; /* process vanished since glob() */
    }

    /* read basic process info */
    if (3 != fscanf(fin, "%ld %s %*c %ld", &(P->pid), P->cmd, &(P->ppid)))
    {
        fclose(fin);
        return 0; /* Problem with file format, AFAIK should not happen */
    }
    if (fstat(file_no(fin), &stat))
    {
        fclose(fin);
        return 0;
    }
    P->uid = stat.st_uid;

    /* fin can't be NULL here */
    fclose(fin);
    return 1;
}



/*
 * Read command line parameters for given ${PID}
 */
int read_cmd_line(char *file_name_part, char *cmd)
{
    FILE *fin;
    char filename[MAX_FILENAME];
    int c;
    int k = 0;

    /* try to open file /proc/${PID}/cmdline for reading */
    snprintf(filename, sizeof(filename), "%s%s", file_name_part, "/cmdline");
    fin = fopen(filename, "r");

    if (fin == NULL)
    {
        return 0; /* process vanished since glob() */
    }

    /* replace \0 by spaces */
    while (k < MAX_LINE - 1 && EOF != (c = fgetc(fin)))
    {
        cmd[k++] = c == '\0' ? ' ' : c;
    }
    if (k > 0)
    {
        cmd[k] = '\0';
    }

    /* fin can't be NULL here */
    fclose(fin);
    return 1;
}



/*
 * Fill in an array pointed by P.
 */
int get_processes(void)
{
    glob_t globbuf;
    unsigned int i, j;

    glob("/proc/[0-9]*", GLOB_NOSORT, NULL, &globbuf);

    P = calloc(globbuf.gl_pathc, sizeof(struct ProcStruct));
    if (P == NULL)
    {
        fprintf(stderr, "Problems with malloc, it should not happen...\n");
        exit(1);
    }

    for (i = j = 0; i < globbuf.gl_pathc; i++)
    {
        char * name_part = globbuf.gl_pathv[globbuf.gl_pathc - i - 1];
        if (read_process_info(name_part, &(P[j])) == 0)
        {
            continue;
        }
        if (read_cmd_line(name_part, P[j].cmd) == 0)
        {
            continue;
        }
        /* Debug output */
        /* printf("uid=%5ld, pid=%5ld, ppid=%5ld, cmd='%s'\n", P[j].uid, P[j].pid, P[j].ppid, P[j].cmd); */
        j++;
    }
    globfree(&globbuf);
    return j;
}



/*
 * Try to open X Display
 */
Display * open_display(void)
{
    Display *display = XOpenDisplay(0);
    if (display == NULL)
    {
        puts("Cannot open display");
        exit(EXIT_CODE_ERROR);
    }
    return display;
}



/*
 * Return the atom identifier for the atom name "_NET_WM_PID"
 */
Atom get_atom_pid(Display *display)
{
    Atom atom_pid = XInternAtom(display, "_NET_WM_PID", True);
    if (atom_pid == None)
    {
        printf("No such atom _NET_WM_PID");
        exit(EXIT_CODE_ERROR);
    }
    return atom_pid;
}



/*
 * Try to focus the window and send Ctrl+W to it.
 */
void close_window(Window window, long processId)
{
    char windowIDstr[MAX_LONG_DECIMAL_DIGITS];
    pid_t pid;

    snprintf(windowIDstr, MAX_LONG_DECIMAL_DIGITS, "%ld", window);
    char *args[] =
    {
        "/usr/bin/xdotool",
        "windowfocus",
        windowIDstr,
        "windowactivate",
        windowIDstr,
        "key",
        "--window",
        windowIDstr,
        "--clearmodifiers",
        "Ctrl+W",
        (char *) NULL
    };
    if ((pid = fork()) == -1)
    {
        perror("some fork error");
    }
    else if (pid == 0)
    {
        /* child process */
        printf("Trying to close window ID %ld for process ID %ld\n", (long)window, processId);
        execv("/usr/bin/xdotool", args);
    }
    else
    {
        /* parent process */
        sleep(SLEEP_AMOUNT);
    }
}



/*
 * Run xkill to kill window with specified window ID
 */
void kill_window(Window window, long processId)
{
    char windowIDstr[MAX_LONG_DECIMAL_DIGITS];
    pid_t pid;

    /* we need to convert window id (long) into a string to call xkill */
    snprintf(windowIDstr, MAX_LONG_DECIMAL_DIGITS, "%ld", window);
    char *args[] =
    {
        "/usr/bin/xkill",
        "-id",
        windowIDstr,
        (char *) NULL
    };
    if ((pid = fork()) == -1)
    {
        perror("some fork error");
    }
    else if (pid == 0)
    {
        printf("Trying to kill window ID %ld for process ID %ld\n", (long)window, processId);
        execv("/usr/bin/xkill", args);
    }
    else
    {
        // parent
        sleep(SLEEP_AMOUNT);
    }
}



/*
 * Recursivelly search for a window(s) associated with given process ID
 */
void search_and_destroy(Display *display, Window window, Atom atomPID, long processId)
{
    Atom           type;
    int            format;
    unsigned long  nItems;
    unsigned long  bytesAfter;
    unsigned char *propertyPID = NULL;

    /* read _NET_WM_PID property, if exists */
    if (Success == XGetWindowProperty(display, window, atomPID, 0, 1, False, XA_CARDINAL,
                                         &type, &format, &nItems, &bytesAfter, &propertyPID))
    {
        if (propertyPID != NULL)
        {
            if (processId == *((unsigned long *)propertyPID))
            {
                printf("Found window ID %ld for process ID %ld\n", (long)window, processId);
                XFree(propertyPID);
#if TRY_TO_CLOSE_WINDOW == 1
                close_window(window, processId);
#endif
#if TRY_TO_KILL_WINDOW == 1
                kill_window(window, processId);
#endif
            }
        }
    }

    /* recurse into child windows */
    Window    rootWindow;
    Window    parentWindow;
    Window   *childWindows;
    unsigned  nChildren;
    if (0 != XQueryTree(display, window, &rootWindow, &parentWindow, &childWindows, &nChildren))
    {
        unsigned int i;
        for(i = 0; i < nChildren; i++)
        {
            search_and_destroy(display, childWindows[i], atomPID, processId);
        }
    }
}



/*
 * Kill process with given ancestor PID.
 */
void kill_process(int pid)
{
    Atom atom_pid = get_atom_pid(display);
    printf("Searching for windows associated with PID %d\n", pid);
    search_and_destroy(display, root_window, atom_pid, pid);
}



/*
 * Kill all processes with given ppid (ancestor PID)
 */
void kill_processes_with_ppid(int ppid)
{
    int i;
    int done = 1;
    for (i = 0; i < N; i++)
    {
        if (ppid == P[i].ppid)
        {
            int pid = P[i].pid;
            printf("uid=%5ld, pid=%5ld, ppid=%5ld, cmd='%s'\n", P[i].uid, P[i].pid, P[i].ppid, P[i].cmd);
            kill_processes_with_ppid(pid);
            /* at least one child process exists */
            done = 0;
            kill_process(pid);
        }
    }
    kill_process(ppid);
    /* if none child processes have been found we are at bottom of the process tree */
    if (done)
    {
        return;
    }
}



/* TODO: better check for user input */
int read_pid(int argc, char **argv)
{
    int pid;

    if (argc != 2)
    {
        puts("Usage softkiller PID");
        exit(EXIT_CODE_ERROR);
    }

    pid = atoi(argv[1]);

    if (sscanf(argv[1], "%d", &pid) != 1)
    {
        printf("Wrong PID entered: %s\n", argv[1]);
        exit(EXIT_CODE_ERROR);
    }

    /* basic check (nobody should try to kill PID 0 :) */
    if (pid <= 0)
    {
        printf("Wrong process ID %d\n", pid);
        exit(EXIT_CODE_ERROR);
    }

    return pid;
}



/*
 * Entry point to this tool.
 */
int main(int argc, char **argv)
{
    int pid = read_pid(argc, argv);

    printf("ancestor PID to kill: %d\n", pid);

    N = get_processes();
    printf("N = %d\n", N);
    display = open_display();
    root_window = XDefaultRootWindow(display);
    atom_pid = get_atom_pid(display);

    kill_processes_with_ppid(pid);

    puts("*** Done! ***");
    return 0;
}

