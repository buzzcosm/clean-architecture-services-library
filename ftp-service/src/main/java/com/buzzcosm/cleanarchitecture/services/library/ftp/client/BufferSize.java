package com.buzzcosm.cleanarchitecture.services.library.ftp.client;

public enum BufferSize {
    BUFFER_8KB(8 * 1024),
    BUFFER_16KB(16 * 1024);

    private final int size;

    BufferSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
