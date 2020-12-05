//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_RACK_H
#define AAUDIOTRACK_RACK_H

template <typename Type> class Rack {

public:
    std::vector<Type *> typeList;

    Type *newType() {
        Type * type = new Type();
        typeList.push_back(type);
        return type;
    }

    void removeType(Type * type) {
        for (auto it = typeList.begin(); it != typeList.end(); it++) {
            if (*it == type) {
                typeList.erase(it);
                break;
            }
        }
        delete type;
    }
};

#endif //AAUDIOTRACK_RACK_H
