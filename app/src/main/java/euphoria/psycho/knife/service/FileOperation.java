package euphoria.psycho.knife.service;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import euphoria.psycho.knife.service.FileOperationService.OpType;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeInt(mOpType);
        out.writeList(mSource);
        out.writeString(mDestination);
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
            return null;
        }
    }
}
