package com.vividsolutions.jump.util;

/**
 * Simply a chunk of code that can be passed around. Facilitates 
 * Smalltalk-like programming. Also useful as a "lexical closure"
 * i.e. a chunk of code with variables having long lifetimes.
 * <p>
 * Typically only one of the #yield methods needs to be implemented.
 * Which one depends on the context.
 */
public abstract class Block {
    public Object yield(Object arg1, Object arg2) {
        throw new UnsupportedOperationException();
    }
    public Object yield(Object arg) {
        throw new UnsupportedOperationException();            
    }
    public Object yield() {
        throw new UnsupportedOperationException();            
    }    
}