#include "libs/env.h"

#include <string>
#include <string>
#include <iostream>
#include <filesystem>
#include <libgen.h>
#include <sys/mount.h>

using namespace std;

string ROOT;
char * COMPONENT = nullptr;
char * OUT = nullptr;

void printHelp(char * program_name) {
    cout << program_name << " -c <SDK_COMPONENT> -o <LOCATION_TO_COPY_INTO>" << endl;
    cout << "-l, --list         list components available" << endl;
    cout << "-h, --help         display this help" << endl;
    cout << "-r, --root         the parent folder that the sdk resides in" << endl;
    cout << "-c, --component    the SDK Component to copy" << endl;
    cout << "-o, --out          the directory to copy the component into" << endl;
}

#define ITERATE_BEGIN(r, full_path, base_name) \
filesystem::directory_iterator directoryIterator = filesystem::directory_iterator( \
        r + "/AndroidDAW_SDK" \
); \
 \
for (const auto & entry : directoryIterator) { \
string full_path = entry.path(); \
string base_name = basename(const_cast<char*>(full_path.c_str())); \
 \
/* ignore these files */ \
if (base_name == "CMAKE_HELPER") continue; \
if (base_name == "consumer-rules.pro") continue; \
if (base_name == "proguard-rules.pro") continue; \
if (base_name == "README.md") continue; \
if (base_name == "build") continue; \
if (base_name == "src") continue; \
if (base_name == ".git") continue; \
if (base_name == ".gitmodules") continue; \
if (base_name == ".gitignore") continue; \
if (base_name == "build.gradle") continue; \

#define ITERATE_END }

void list_components() {
    cout << "Components: " << endl;
    ITERATE_BEGIN(ROOT, a, b)
        cout << "    " << b << endl;
    ITERATE_END
}

int main(int argc, env_t argv) {
    const char * help = env__get_name(argv, "--help");
    const char * h = env__get_name(argv, "-h");
    const char * list = env__get_name(argv, "--list");
    const char * l = env__get_name(argv, "-l");
    const char * root = env__get_name(argv, "--root");
    const char * r = env__get_name(argv, "-r");
    const char * component = env__get_name(argv, "--component");
    const char * c = env__get_name(argv, "-c");
    const char * out = env__get_name(argv, "--out");
    const char * o = env__get_name(argv, "-o");

    if (argc == 1 || h || help) {
        printHelp(argv[0]);
        return 0;
    }

    if (r || root) {
        if (r) {
            int outPos = env__getposition(argv, r);
            if (argc == outPos) {
                cerr << "error: a root must be specified" << endl;
                printHelp(argv[0]);
                return 1;
            }
            ROOT = argv[outPos];
        }
        if (root) {
            int outPos = env__getposition(argv, root);
            if (argc == outPos) {
                cerr << "error: a root must be specified" << endl;
                printHelp(argv[0]);
                return 1;
            }
            ROOT = argv[outPos];
        }
    } else {
        ROOT = ".";
    }

    if (l || list) {
        list_components();
        return 0;
    }

    if (c || component) {
        if (c) {
            int outPos = env__getposition(argv, c);
            if (argc == outPos) {
                cerr << "error: an SDK Component must be specified" << endl;
                list_components();
                return 1;
            }
            COMPONENT = argv[outPos];
            if (strlen(COMPONENT) == 0) {
                cerr << "error: empty SDK Component specified" << endl;
                return 1;
            }
        }
        if (component) {
            int outPos = env__getposition(argv, component);
            if (argc == outPos) {
                cerr << "error: an SDK Component must be specified" << endl;
                list_components();
                return 1;
            }
            COMPONENT = argv[outPos];
            if (strlen(COMPONENT) == 0) {
                cerr << "error: empty SDK Component specified" << endl;
                return 1;
            }
        }
    } else {
        cerr << "error: an SDK Component must be specified" << endl;
        list_components();
        return 1;
    }

    if (o || out) {
        if (o) {
            int outPos = env__getposition(argv, o);
            if (argc == outPos) {
                cerr << "error: an output directory must be specified" << endl;
                printHelp(argv[0]);
                return 1;
            }
            OUT = argv[outPos];
            if (strlen(OUT) == 0) {
                cerr << "error: empty output directory specified" << endl;
                return 1;
            }
        }
        if (out) {
            int outPos = env__getposition(argv, out);
            if (argc == outPos) {
                cerr << "error: an output directory must be specified" << endl;
                printHelp(argv[0]);
                return 1;
            }
            OUT = argv[outPos];
            if (strlen(OUT) == 0) {
                cerr << "error: empty output directory specified" << endl;
                return 1;
            }
        }
    } else {
        cerr << "error: an output directory must be specified" << endl;
        printHelp(argv[0]);
        return 1;
    }

    string final = string(OUT) + "/" + COMPONENT;

    if (!filesystem::create_directory(final)) {
        cerr << "failed to create output directory: " << final << endl;
        return 1;
    }

    ITERATE_BEGIN(ROOT, full, base)
        if (base == COMPONENT) {
            cout << "found component: " << base << endl;
            cout << "copying " << base << " to " << final << endl;
            string cmd = "cp -R";
            cmd += " ";
            cmd += full;
            cmd += " ";
            cmd += final;
            cout << "executing command: " << cmd << endl;
            int ret = system(cmd.c_str());
            cout << "executed command (return code: " << ret << ") : " << cmd << endl;
            return ret;
        }
    ITERATE_END
    cerr << "invalid component specified: " << COMPONENT << endl;
    list_components();
    return 1;
}