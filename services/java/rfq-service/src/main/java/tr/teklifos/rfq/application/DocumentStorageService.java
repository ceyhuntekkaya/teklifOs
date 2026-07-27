package tr.teklifos.rfq.application;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import tr.teklifos.rfq.config.StorageProperties;

@Service
public class DocumentStorageService {

    private final MinioClient client;
    private final StorageProperties props;

    public DocumentStorageService(MinioClient client, StorageProperties props) {
        this.client = client;
        this.props = props;
    }

    public String originalKey(UUID tenantId, UUID rfqId, UUID documentId, String fileName) {
        String safeName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "tenant/%s/rfq/%s/%s/original/%s"
                .formatted(tenantId, rfqId, documentId, safeName);
    }

    public void putOriginal(
            String key, InputStream stream, long size, String contentType) throws Exception {
        ensureBucket();
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(props.bucket())
                        .object(key)
                        .stream(stream, size, -1)
                        .contentType(contentType)
                        .build());
    }

    public String presignedGetUrl(String key) throws Exception {
        return client.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(props.bucket())
                        .object(key)
                        .expiry(1, TimeUnit.HOURS)
                        .build());
    }

    private void ensureBucket() throws Exception {
        boolean exists =
                client.bucketExists(
                        io.minio.BucketExistsArgs.builder().bucket(props.bucket()).build());
        if (!exists) {
            client.makeBucket(
                    io.minio.MakeBucketArgs.builder().bucket(props.bucket()).build());
        }
    }
}
