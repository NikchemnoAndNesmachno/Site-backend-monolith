import { useEffect, useMemo, useRef, useState } from "react";
import "../styles/video.css";

type Props = {
    src: string;
    poster?: string | null;
    title: string;
};

function formatTime(seconds: number) {
    if (!Number.isFinite(seconds) || seconds < 0) {
        return "0:00";
    }

    const totalSeconds = Math.floor(seconds);
    const minutes = Math.floor(totalSeconds / 60);
    const remainingSeconds = totalSeconds % 60;

    return `${minutes}:${String(remainingSeconds).padStart(2, "0")}`;
}

export function VideoPlayer({ src, poster, title }: Props) {
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [progress, setProgress] = useState(0);
    const [duration, setDuration] = useState(0);
    const [volume, setVolume] = useState(1);
    const [isSeeking, setIsSeeking] = useState(false);

    useEffect(() => {
        const video = videoRef.current;
        if (!video) {
            return;
        }

        const syncPlayingState = () => setIsPlaying(!video.paused);
        const syncMetadata = () => setDuration(video.duration || 0);
        const syncProgress = () => {
            if (!isSeeking) {
                setProgress(video.currentTime);
            }
        };
        const onEnded = () => {
            setIsPlaying(false);
            setProgress(0);
        };

        video.addEventListener("play", syncPlayingState);
        video.addEventListener("pause", syncPlayingState);
        video.addEventListener("loadedmetadata", syncMetadata);
        video.addEventListener("timeupdate", syncProgress);
        video.addEventListener("ended", onEnded);

        return () => {
            video.removeEventListener("play", syncPlayingState);
            video.removeEventListener("pause", syncPlayingState);
            video.removeEventListener("loadedmetadata", syncMetadata);
            video.removeEventListener("timeupdate", syncProgress);
            video.removeEventListener("ended", onEnded);
        };
    }, [isSeeking]);

    const progressPercent = useMemo(() => {
        if (!duration) {
            return 0;
        }

        return (progress / duration) * 100;
    }, [duration, progress]);

    async function handlePlayPause() {
        const video = videoRef.current;
        if (!video) {
            return;
        }

        if (video.paused) {
            await video.play();
        } else {
            video.pause();
        }
    }

    function handleSeekChange(nextValue: number) {
        setIsSeeking(true);
        setProgress(nextValue);
    }

    function handleSeekCommit(nextValue: number) {
        const video = videoRef.current;
        if (!video) {
            return;
        }

        video.currentTime = nextValue;
        setProgress(nextValue);
        setIsSeeking(false);
    }

    function handleVolumeChange(nextValue: number) {
        const normalized = nextValue / 100;
        setVolume(normalized);

        if (videoRef.current) {
            videoRef.current.volume = normalized;
        }
    }

    return (
        <section className="video-player" aria-label="video player">
            <div className="video-player__surface">
                <video
                    ref={videoRef}
                    className="video-player__media"
                    src={src}
                    poster={poster ?? undefined}
                    preload="metadata"
                    playsInline
                    onClick={() => void handlePlayPause()}
                />

                <div className="video-player__controls">
                    <button
                        type="button"
                        className="video-player__button"
                        onClick={() => void handlePlayPause()}
                        aria-label={isPlaying ? "Pause video" : "Play video"}
                    >
                        {isPlaying ? "❚❚" : "▶"}
                    </button>

                    <div className="video-player__progress-group">
                        <input
                            className="video-player__range video-player__range--progress"
                            type="range"
                            min={0}
                            max={duration || 0}
                            step={0.1}
                            value={progress}
                            onChange={(event) => handleSeekChange(Number(event.target.value))}
                            onMouseUp={(event) => handleSeekCommit(Number((event.target as HTMLInputElement).value))}
                            onTouchEnd={(event) => handleSeekCommit(Number((event.target as HTMLInputElement).value))}
                            aria-label="Seek video"
                        />
                        <div className="video-player__time-row">
                            <span>{formatTime(progress)}</span>
                            <span>{formatTime(duration)}</span>
                        </div>
                    </div>

                    <label className="video-player__volume" aria-label="Volume control">
                        <span>🔊</span>
                        <input
                            className="video-player__range video-player__range--volume"
                            type="range"
                            min={0}
                            max={100}
                            step={1}
                            value={volume * 100}
                            onChange={(event) => handleVolumeChange(Number(event.target.value))}
                        />
                    </label>
                </div>
            </div>

            <div className="video-player__meta-row">
                <span className="video-player__meta-label">Now playing</span>
                <strong>{title}</strong>
                <span>{progressPercent.toFixed(0)}%</span>
            </div>
        </section>
    );
}
