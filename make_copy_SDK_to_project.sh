set -v
g++ \
    bin/libs/env.cpp \
    -o bin/libs/env.so \
    --shared -fPIC

g++ \
    --std=c++17 \
    bin/copy_SDK_to_project.cpp \
    -o bin/copy_SDK_to_project \
    ./bin/libs/env.so