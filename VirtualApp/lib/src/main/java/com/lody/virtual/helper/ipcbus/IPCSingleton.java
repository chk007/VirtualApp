package com.lody.virtual.helper.ipcbus;

/**
 * 每个VA Service在每个VA进程中，都具备一个IPCSingleton对象
 * 注: 面向泛型编程
 *
 * @Server
 * @VAppClient
 * @author Lody
 */
public class IPCSingleton<T> {

    private Class<?> ipcClass;
    private T instance;

    public IPCSingleton(Class<?> ipcClass) {
        this.ipcClass = ipcClass;
    }
    
    /**
     * 获取VA Service入口. 获取指定类型的VA Service
     * @VAppClient
     * @Service
     * @return VA Service single instance
     */
    public T get() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = IPCBus.get(ipcClass);
                }
            }
        }
        return instance;
    }

}
