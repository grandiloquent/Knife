package euphoria.psycho.knife.service;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import euphoria.psycho.knife.service.FileOperationService.OpType;
import euphoria.psycho.knife.util.CollectionUtils;

import static euphoria.psycho.knife.service.FileOperationService.OPERATION_COMPRESS;
import static euphoria.psycho.knife.service.FileOperationService.OPERATION_COPY;
import static euphoria.psycho.knife.service.FileOperationService.OPERATION_DELETE;
import static euphoria.psycho.knife.service.FileOperationService.OPERATION_EXTRACT;
import static euphoria.psycho.knife.service.FileOperationService.OPERATION_MOVE;

public abstract class FileOperation implements Parcelable {

    private final String mDestination;
    private final @OpType
    int mOpType;
    private final List<String> mSource;

    FileOperation(@OpType int opType, List<String> source, String destination) {
        mSource = source;
        mDestination = destination;
        mOpType = opType;
    }

    private FileOperation(Parcel in) {
        mOpType = in.readInt();
        mSource = in.readArrayList(FileOperation.class.getClassLoader());
        mDestination = in.readString();
    }

    abstract Job createJob(Context service, Job.Listener listener, String id);

    public int getOpType() {
        return mOpType;
    }

    public List<String> getSource() {
        return mSource;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "FileOperation{" +
                "mDestination='" + mDestination + '\'' +
                ", mOpType=" + mOpType +
                ", mSource=" + CollectionUtils.toString(mSource) +
                '}';
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeInt(mOpType);
        out.writeList(mSource);
        out.writeString(mDestination);
    }

    public static class Builder {
        private String mDestination;
        private @OpType
        int mOpType;
        private List<String> mSource;

        public FileOperation build() {
            switch (mOpType) {
                case OPERATION_COPY:
                    return null;
                case OPERATION_COMPRESS:
                    return null;
                case OPERATION_EXTRACT:
                    return null;
                case OPERATION_MOVE:
                case OPERATION_DELETE:
                    return new MoveDeleteOperation(mOpType, mSource, mDestination);
                default:
                    throw new UnsupportedOperationException("Unsupported op type: " + mOpType);
            }
        }

        public Builder withDestination(String destination) {
            mDestination = destination;
            return this;
        }

        public Builder withOpType(@OpType int opType) {
            mOpType = opType;
            return this;
        }

        public Builder withSource(List<String> source) {
            mSource = source;
            return this;
        }


    }

    public static class MoveDeleteOperation extends FileOperation {


        public static final Parcelable.Creator<MoveDeleteOperation> CREATOR =
                new Parcelable.Creator<MoveDeleteOperation>() {


                    @Override
                    public MoveDeleteOperation createFromParcel(Parcel source) {
                        return new MoveDeleteOperation(source);
                    }

                    @Override
                    public MoveDeleteOperation[] newArray(int size) {
                        return new MoveDeleteOperation[size];
                    }
                };

        MoveDeleteOperation(int opType, List<String> source, String destination) {
            super(opType, source, destination);
        }

        private MoveDeleteOperation(Parcel in) {
            super(in);
        }

        @Override
        Job createJob(Context service, Job.Listener listener, String id) {
            switch (getOpType()) {
                case OPERATION_DELETE:
                    return null;
            }
            return null;
        }
    }
}
