package smallville7123.aaudiotrack2;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class AAudioTrack2 {
    static {
        System.loadLibrary("AAudioTrack2");
    }

    private native void createNativeInstance();
    private native void startEngine();
    private native void stopEngine();
    public native void setNoteData(long pattern, boolean[] array);
    public native int getDSPLoad();
    public native void bindChannelToPattern(long channel, long pattern);
    public native long createPatternList();
    public native void deletePatternList(long patternList);
    public native long createPattern(long patternList);
    public native void deletePattern(long patternList, long pattern);

    public native boolean isNotePlaying(int noteDataIndex);

    public  native int getSampleRate();
    public  native int getChannelCount();
    public  native int getUnderrunCount();
    public  native int getCurrentFrame();
    public  native int getTotalFrames();
    public  native void setTrack(long channelID, String track);
    public  native void resetPlayHead();
    public  native void pause();
    public  native void resume();
    public  native void loop(boolean value);
    public  native long newChannel();
    public  native long newSamplerChannel();

    private String converted;

    public AAudioTrack2() {
        createNativeInstance();
        startEngine();
    }

    // each Audio Track instance will correspond to a Channel in the Channel Rack

    private void _load(long channelID, Path tmp) {
        int sampleRate = getSampleRate();
        int channelCount = getChannelCount();
        converted = tmp + ".converted.f_s16le.ar_" + sampleRate + ".ac_" + channelCount;
        int returnCode = FFmpeg.execute("-y" + " " +
                // input
                "-i " + tmp + " " +
                // output
                "-f s16le" + " " +// audio format is signed 16 bit little-endian pcm
                "-ar " + sampleRate + " " +
                "-ac " + channelCount + " " +
                converted
        );
        if (returnCode == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
            setTrack(channelID, converted);
        } else {
            throw new RuntimeException(Config.TAG + ": Command execution failed (returned " + returnCode + ")");
        }
    }

    /**
     * Load the sound from the specified path.
     *
     * @param channelID the channel to load the audio into
     * @param context this is used to obtain a temporary directory to decode the audio file to
     * @param path the path to the audio file
     */
    public void loadPath(long channelID, Context context, String path) {
        CharSequence extension = path.substring(path.lastIndexOf("."));
        File out = createTemporaryFile(context, extension);
        Path outPath = out.toPath();
        try {
            Files.copy(Paths.get(path), outPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("error loading " + path + ": " + e);
        }
        _load(channelID, outPath);
    }

    /**
     * Load the sound from the specified APK resource.
     *
     * Note that the extension is dropped. For example, if you want to load
     * a sound from the raw resource file "explosion.mp3", you would specify
     * "R.raw.explosion" as the resource ID. Note that this means you cannot
     * have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     * directory.
     *
     * @param channelID the channel to load the audio into
     * @param context this is used to obtain a temporary directory to decode the audio file to
     * @param resId the resource ID
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     */
    public void load(long channelID, Context context, int resId, CharSequence extension) {
        load(channelID, context, context.getResources().openRawResourceFd(resId), extension);
    }

    /**
     * Load the sound from an asset file descriptor.
     *
     * @param channelID the channel to load the audio into
     * @param context this is used to obtain a temporary directory to decode the audio file to
     * @param afd an asset file descriptor
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     */
    public void load(long channelID, Context context, AssetFileDescriptor afd, CharSequence extension) {
        Objects.requireNonNull(afd);
        File out = createTemporaryFile(context, extension);
        Path outPath = out.toPath();
        Utils.copy(afd, outPath);
        _load(channelID, outPath);
    }

    private File createTemporaryFile(Context context, CharSequence extension) {
        try {
            return File.createTempFile("TMP_", extension.toString(), context.getFilesDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the sound from a FileDescriptor.
     *
     * @param channelID the channel to load the audio into
     * @param context this is used to obtain a temporary directory to decode the audio file to
     * @param fd a FileDescriptor object
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     */
    public void load(long channelID, Context context, FileDescriptor fd, CharSequence extension) {
        File out = createTemporaryFile(context, extension);
        Path outPath = out.toPath();
        Utils.copy(fd, outPath);
        _load(channelID, outPath);
    }

    /**
     * Load the sound from a FileDescriptor.
     *
     * This version is useful if you store multiple sounds in a single
     * binary. The offset specifies the offset from the start of the file
     * and the length specifies the length of the sound within the file.
     *
     * @param channelID the channel to load the audio into
     * @param context this is used to obtain a temporary directory to decode the audio file to
     * @param fd a FileDescriptor object
     * @param offset offset to the start of the sound
     * @param length length of the sound
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     */
    public void load(long channelID, Context context, FileDescriptor fd, long offset, long length, CharSequence extension) {
        File out = createTemporaryFile(context, extension);
        Path outPath = out.toPath();
        try {
            FileInputStream fis = new FileInputStream(fd);
            Utils.copy(fis, outPath, offset, length, StandardCopyOption.REPLACE_EXISTING);
            fis.close();
        } catch (IOException ex) {
            throw new RuntimeException("close failed: " + ex);
        }
        _load(channelID, outPath);
    }

    private static class Utils {

        // buffer size used for reading and writing
        private static final int BUFFER_SIZE = 8192;

        private static void copy(FileDescriptor fd, Path outPath) {
            copy(new FileInputStream(fd), outPath);
        }

        private static void copy(FileInputStream fis, Path outPath) {
            try {
                Files.copy(fis, outPath, StandardCopyOption.REPLACE_EXISTING);
                fis.close();
            } catch (IOException ex) {
                throw new RuntimeException("close failed: " + ex);
            }
        }

        private static void copy(AssetFileDescriptor fd, Path outPath) {
            Objects.requireNonNull(fd);
            try {
                copy(fd.createInputStream(), outPath);
                fd.close();
            } catch (IOException ex) {
                throw new RuntimeException("close failed: " + ex);
            }
        }

        /**
         * Reads all bytes from an input stream and writes them to an output stream.
         */
        private static long copy(InputStream source, long offset, long length, OutputStream sink)
                throws IOException
        {
            source.skip(offset);
            long nread = 0L;
            byte[] buf = new byte[BUFFER_SIZE];
            int n = 0;
            while ((n = source.read(buf, 0, Math.min(BUFFER_SIZE, (int) (length - n)))) > 0) {
                sink.write(buf, 0, n);
                nread += n;
            }
            return nread;
        }

        /**
         * Copies all bytes from an input stream to a file. On return, the input
         * stream will be at end of stream.
         *
         * <p> By default, the copy fails if the target file already exists or is a
         * symbolic link. If the {@link StandardCopyOption#REPLACE_EXISTING
         * REPLACE_EXISTING} option is specified, and the target file already exists,
         * then it is replaced if it is not a non-empty directory. If the target
         * file exists and is a symbolic link, then the symbolic link is replaced.
         * In this release, the {@code REPLACE_EXISTING} option is the only option
         * required to be supported by this method. Additional options may be
         * supported in future releases.
         *
         * <p>  If an I/O error occurs reading from the input stream or writing to
         * the file, then it may do so after the target file has been created and
         * after some bytes have been read or written. Consequently the input
         * stream may not be at end of stream and may be in an inconsistent state.
         * It is strongly recommended that the input stream be promptly closed if an
         * I/O error occurs.
         *
         * <p> This method may block indefinitely reading from the input stream (or
         * writing to the file). The behavior for the case that the input stream is
         * <i>asynchronously closed</i> or the thread interrupted during the copy is
         * highly input stream and file system provider specific and therefore not
         * specified.
         *
         * <p> <b>Usage example</b>: Suppose we want to capture a web page and save
         * it to a file:
         * <pre>
         *     Path path = ...
         *     URI u = URI.create("http://java.sun.com/");
         *     try (InputStream in = u.toURL().openStream()) {
         *         Files.copy(in, path);
         *     }
         * </pre>
         *
         * @param   in
         *          the input stream to read from
         * @param   target
         *          the path to the file
         * @param   offset
         *          the offset of the starting location of the input stream
         * @param   length
         *          the length to copy
         * @param   options
         *          options specifying how the copy should be done
         *
         * @return  the number of bytes read or written
         *
         * @throws  IOException
         *          if an I/O error occurs when reading or writing
         * @throws FileAlreadyExistsException
         *          if the target file exists but cannot be replaced because the
         *          {@code REPLACE_EXISTING} option is not specified <i>(optional
         *          specific exception)</i>
         * @throws DirectoryNotEmptyException
         *          the {@code REPLACE_EXISTING} option is specified but the file
         *          cannot be replaced because it is a non-empty directory
         *          <i>(optional specific exception)</i>     *
         * @throws  UnsupportedOperationException
         *          if {@code options} contains a copy option that is not supported
         * @throws  SecurityException
         *          In the case of the default provider, and a security manager is
         *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
         *          method is invoked to check write access to the file. Where the
         *          {@code REPLACE_EXISTING} option is specified, the security
         *          manager's {@link SecurityManager#checkDelete(String) checkDelete}
         *          method is invoked to check that an existing file can be deleted.
         */
        public static long copy(InputStream in, Path target, long offset, long length, CopyOption... options)
                throws IOException
        {
            // ensure not null before opening file
            Objects.requireNonNull(in);

            // check for REPLACE_EXISTING
            boolean replaceExisting = false;
            for (CopyOption opt: options) {
                if (opt == StandardCopyOption.REPLACE_EXISTING) {
                    replaceExisting = true;
                } else {
                    if (opt == null) {
                        throw new NullPointerException("options contains 'null'");
                    }  else {
                        throw new UnsupportedOperationException(opt + " not supported");
                    }
                }
            }

            // attempt to delete an existing file
            SecurityException se = null;
            if (replaceExisting) {
                try {
                    Files.deleteIfExists(target);
                } catch (SecurityException x) {
                    se = x;
                }
            }

            // attempt to create target file. If it fails with
            // FileAlreadyExistsException then it may be because the security
            // manager prevented us from deleting the file, in which case we just
            // throw the SecurityException.
            OutputStream ostream;
            try {
                ostream = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE);
            } catch (FileAlreadyExistsException x) {
                if (se != null)
                    throw se;
                // someone else won the race and created the file
                throw x;
            }

            // do the copy
            try (OutputStream out = ostream) {
                return copy(in, offset, length, out);
            }
        }
    }
}
