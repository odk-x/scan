/** 
 * @file UnionFind.h 
 * This file contains an implementation of the UnionFind data structure 
 * that helps in efficiently finding connected components in just two 
 * passes through an image. 
 * 
 * @author Faisal Shafait (faisalshafait@gmail.com)  
 * 
 * @version 0.1  
 * 
 */ 
 
#ifndef UNIONFIND_H 
#define UNIONFIND_H 
 
#include "stdio.h" 
#include <assert.h>

/** 
 * @class CUnionFind 
 * This class contains an implementation of the UnionFind data structure 
 * that helps in efficiently finding connected components in just two 
 * passes through an image.
*/ 

class CUnionFind {
protected:
    int length;
    int *labels;

public:
    CUnionFind(int n){
        length = n;
        labels = new int[n] ;
        for (int i=0; i<n; i++)
            labels[i]=0;
    }
    ~CUnionFind(){
        length=-1;
        delete[] labels;
    }
    int find(int n){
        assert(n>=0 && n<length);
        int i=n;
        while (labels[i]!=0) {
            i = labels[i];
        }
        if (i!=n) labels[n] = i;
        return i;
    }
    void set(int n, int newVal){
        assert(n>=0 && n<length);
        labels[n] = newVal;
    }
    bool isRoot(int n){
        assert(n>=0 && n<length);
        return (labels[n]==0);
    }			
};

#endif

