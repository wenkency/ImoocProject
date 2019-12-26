package cn.carhouse.audio.state;

/**
 * MediaPlayer状态
 */
public enum MediaStatus {
    // 默认、初始化、准备、开始播放、暂停、停止、完成、错误
    IDLE, INITIALIZED, PREPARED, STARTED, PAUSED, STOPPED, COMPLETED, ERROR,RESET,RELEASE
}
