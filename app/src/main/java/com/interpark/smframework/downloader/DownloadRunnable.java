package com.interpark.smframework.downloader;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.interpark.smframework.IDirector;
import com.interpark.smframework.util.IOUtils;
import com.interpark.smframework.util.NetworkStreamRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadRunnable implements Runnable, Response.Listener<ByteArrayInputStream>, Response.ErrorListener {

    // 한번에 2k씩 읽자.
    private static final int READ_SIZE = 1024 * 2;

    // Sets a tag for this class
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "DownloadRunnable";

    public enum TYPE {
        DATA,
        IMAGE,
        EBOOK,
        MOVIE,
    }

    // Constants for indicating the state of the download
    static final int HTTP_STATE_FAILED = -1;
    static final int HTTP_STATE_STARTED = 0;
    static final int HTTP_STATE_COMPLETED = 1;

    // Defines a field that contains the calling object of type PhotoTask.
    final DownloadRunnableTaskMethods mDownloadTask;
    final Context mContext;
    final IDirector _director;
    private InputStream mByteInputStream;
    private NetworkStreamRequest mRequest;


    // The Android Open Source Project
    interface DownloadRunnableTaskMethods {
        void setDownloadThread(Thread currentThread);
        byte[] getByteBuffer();
        void setByteBuffer(byte[] buffer);
        void handleDownloadState(int state);

        // custom
        int getStorageMediaType();
        String getDownloadedPath();
        String getDiskCachePath();
    }

    DownloadRunnable(IDirector director, DownloadRunnable.DownloadRunnableTaskMethods downloadTask) {
        _director = director;
        mContext = director.getContext();
        mDownloadTask = downloadTask;
        mByteInputStream = null;
        mRequest = null;
    }

    @Override
    public void run() {
        // current Thread를 download Thread로 set
        mDownloadTask.setDownloadThread(Thread.currentThread());

        // current Thread를 background로 보냄
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // downloader로 부터 메모리 캐시된 buffer를 얻어옴
        // 캐시 키 등은 task가 알아서 처리할 것이므로 download thread에서는 몰라도 됨.
        byte[] byteBuffer = mDownloadTask.getByteBuffer();

        try {
            // 돌던게 있으면 멈추고
            if (Thread.interrupted()) {

                throw new InterruptedException();
            }

            // 캐시된게 없으면 새로 받아온다.
            if (null == byteBuffer) {
                // 읽어올 곳이 어디여?
                if (mDownloadTask.getStorageMediaType() == Constants.MEDIA_ASSETS) {
                    // asset?
                    InputStream is = null;
                    try {
                        is = mContext.getAssets().open(mDownloadTask.getDownloadedPath());
                        byteBuffer = IOUtils.toByteArray(is);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeSilently(is);
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    if (byteBuffer != null && byteBuffer.length > 0) {
                        // 읽어 온게 있으면
                        mDownloadTask.setByteBuffer(byteBuffer);
                        mDownloadTask.handleDownloadState(HTTP_STATE_COMPLETED);
                    } else {
                        // 읽어 온게 없으면
                        mDownloadTask.handleDownloadState(HTTP_STATE_FAILED);
                    }
                    return;
                } else if (mDownloadTask.getStorageMediaType() == Constants.MEDIA_SDCARD) {
                    // sdcard?
                    try {
                        byteBuffer = IOUtils.readFile(mDownloadTask.getDownloadedPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    if (byteBuffer != null && byteBuffer.length > 0) {
                        mDownloadTask.setByteBuffer(byteBuffer);
                        mDownloadTask.handleDownloadState(HTTP_STATE_COMPLETED);
                    } else {
                        mDownloadTask.handleDownloadState(HTTP_STATE_FAILED);
                    }
                    return;

                } else {
                    // 그마저도 아니면 캐시에서 읽는다.
                    if (mDownloadTask.getDiskCachePath() != null) {
                        try {
                            // disk cache에서 read file로 읽어온다
                            byteBuffer = IOUtils.readFile(mDownloadTask.getDiskCachePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (Thread.interrupted()) {

                            throw new InterruptedException();
                        }
                        if (byteBuffer != null && byteBuffer.length > 0) {
                            mDownloadTask.setByteBuffer(byteBuffer);
                            mDownloadTask.handleDownloadState(HTTP_STATE_COMPLETED);
                            return;
                        } else {
                            // 캐시는 어디까지나 캐시니까 못읽었다 해서 failed처리 하지 않는다.
                            // 못읽은 경우 네트웍에 내려 받는다.
                        }
                    }
                }

                // asset도 아니고 sdcard도 아니고 diskcache도 아니 네트웍이다.

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                // 내려 받기 시작
                mDownloadTask.handleDownloadState(HTTP_STATE_STARTED);

                // init input stream
                InputStream byteStream = null;

                mRequest = new NetworkStreamRequest(mDownloadTask.getDownloadedPath(), this, this);
                _director.getRequestQueue().add(mRequest);

                synchronized (DownloadRunnable.this) {
                    // 기다리다
                    wait();

                    // 다 읽었으면
                    byteStream = mByteInputStream;
                }

                if (byteStream == null) {
                    // 못 읽어 옴.
                    throw new InterruptedException();
                }

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                try {
                    int contentSize = -1;

                    // 2K 씩 읽어온다.
                    byte[] tempBuffer = new byte[READ_SIZE];
                    int bufferLeft = tempBuffer.length;

                    int bufferOffset = 0;
                    int readResult = 0;

                    // 바깥 while
                    outer: do {
                        // 안쪽 while
                        while (bufferLeft > 0) {
                            readResult = byteStream.read(tempBuffer, bufferOffset, bufferLeft);

                            if (readResult < 0) {
                                // 더이상 읽을 것이 없음... out while을 빠져나감
                                break outer;
                            }

                            // offset 이동
                            bufferOffset += readResult;

                            // 남은거..
                            bufferLeft -= readResult;

                            if (Thread.interrupted()) {

                                throw new InterruptedException();
                            }
                        }

                        // bufferLef가 0보다 작거나 같으면... 더이상 읽을게 없으면...

                        // 다음 블럭 단위를 위해서 남은 bufferLeft를 초기화 한다.
                        bufferLeft = READ_SIZE;

                        int newSize = tempBuffer.length + READ_SIZE;

                        byte[] expandedBuffer = new byte[newSize];
                        System.arraycopy(tempBuffer, 0, expandedBuffer, 0, tempBuffer.length);
                        tempBuffer = expandedBuffer;
                    } while (true);

                    // 다 읽었으면, 마지막 offset을 크기의 buffer에 담는다.
                    byteBuffer = new byte[bufferOffset];

                    System.arraycopy(tempBuffer, 0, byteBuffer, 0, bufferOffset);

                    // 읽은거를 파일 캐시에 저장 해 보자
                    if (byteBuffer != null && mDownloadTask.getDiskCachePath() != null) {
                        // disk cache path가 있을 경우 저장
                        final byte[] diskCacheBuffer = byteBuffer;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    IOUtils.writeFile(diskCacheBuffer, mDownloadTask.getDiskCachePath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                    // 다 끝났으면 쓰레드 종료
                    if (Thread.interrupted()) {

                        throw new InterruptedException();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                } finally {
                    // 다 끝났으면
                    // request 초기화
                    if (mRequest != null) {
                        synchronized (DownloadRunnable.this) {
                            if (mRequest != null) {
                                mRequest.cancel();
                                mRequest = null;
                            }
                        }
                    }
                    if (null != byteStream) {
                        try {
                            // close input sream
                            byteStream.close();
                        } catch (Exception e) {
                            // Does nothing
                        }
                    }
                }
            }

            // 끝났으면 buffer를 돌려준다.
            mDownloadTask.setByteBuffer(byteBuffer);
            // 끝났음을 알린다.
            mDownloadTask.handleDownloadState(HTTP_STATE_COMPLETED);

        } catch (InterruptedException e1) {

        } finally {
            // 끝내자... request를 비운다
            if (mRequest != null) {
                synchronized (DownloadRunnable.this) {
                    if (mRequest != null) {
                        mRequest.cancel();
                        mRequest = null;
                    }
                }
            }

            if (null == byteBuffer) {
                // 받은게 없으면 실패
                mDownloadTask.handleDownloadState(HTTP_STATE_FAILED);
            }

            // 다 끝냇으면 download thread를 비운다.
            mDownloadTask.setDownloadThread(null);

            // Clears the Thread's interrupt flag
            Thread.interrupted();
        }
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        synchronized (DownloadRunnable.this) {
            mRequest = null;
            mByteInputStream = null;
            notifyAll();
        }
    }

    @Override
    public void onResponse(ByteArrayInputStream response) {
        synchronized (DownloadRunnable.this) {
            mRequest = null;
            mByteInputStream = response;
            notifyAll();
        }
    }

}
