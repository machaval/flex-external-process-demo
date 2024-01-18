package github.machaval.transformation.grpc;

import com.google.protobuf.ByteString;
import github.machaval.transformation.config.TransformationManager;
import github.machaval.transformation.config.TransformationScript;
import github.machaval.transformation.utils.Utils;
import io.envoyproxy.envoy.config.core.v3.HeaderMap;
import io.envoyproxy.envoy.extensions.filters.http.ext_proc.v3.ProcessingMode;
import io.envoyproxy.envoy.service.ext_proc.v3.*;
import io.grpc.stub.StreamObserver;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScript;
import org.mule.weave.v2.runtime.ScriptingBindings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GRPCTransformationServiceImpl extends ExternalProcessorGrpc.ExternalProcessorImplBase {
    static Logger LOG = Logger.getLogger(GRPCTransformationServiceImpl.class.getSimpleName());

    final TransformationManager transformationManager;

    public GRPCTransformationServiceImpl(String path) {
        transformationManager = TransformationManager.loadFrom(path);
    }


    @Override
    public StreamObserver<ProcessingRequest> process(StreamObserver<ProcessingResponse> grpcResponseObserver) {
        return new FilterEventsHandler(grpcResponseObserver, transformationManager);
    }

    private static final class FilterEventsHandler implements StreamObserver<ProcessingRequest> {
        private final StreamObserver<ProcessingResponse> grpcResponseObserver;
        private final TransformationManager transformationManager;
        private DataWeaveScript bodyScript;

        private FilterEventsHandler(StreamObserver<ProcessingResponse> grpcResponseObserver, TransformationManager transformationManager) {
            this.grpcResponseObserver = grpcResponseObserver;
            this.transformationManager = transformationManager;
        }


        @Override
        public void onNext(ProcessingRequest processingRequest) {
            if (processingRequest.hasRequestHeaders()) {
                ProcessingResponse processingResponse = onRequestHeader(processingRequest);
                grpcResponseObserver.onNext(processingResponse);
            } else if (processingRequest.hasRequestBody()) {
                ProcessingResponse processingResponse = onRequestBody(processingRequest);
                grpcResponseObserver.onNext(processingResponse);
            } else if (processingRequest.hasRequestTrailers()) {
                ProcessingResponse processingResponse = onRequestTrailers(processingRequest);
                grpcResponseObserver.onNext(processingResponse);
            } else if (processingRequest.hasResponseHeaders()) {
                ProcessingResponse processingResponse = onResponseHeader(processingRequest);
                grpcResponseObserver.onNext(processingResponse);
            } else if (processingRequest.hasResponseBody()) {
                ProcessingResponse processingResponse = onResponseBody(processingRequest);
                grpcResponseObserver.onNext(processingResponse);
            } else if (processingRequest.hasResponseTrailers()) {
                ProcessingResponse processingResponse = onResponseTrailers(processingRequest);
                grpcResponseObserver.onNext(processingResponse);
            }
        }

        private ProcessingResponse onRequestHeader(ProcessingRequest processingRequest) {
            HeaderMap headers = processingRequest.getRequestHeaders().getHeaders();
            LOG.log(Level.INFO, "On Request Headers: " + headers.getHeadersList().size());
            ProcessingResponse.Builder builder = ProcessingResponse.newBuilder();
            HashMap<String, String> headersAsMap = Utils.headersAsMap(headers);
            Optional<TransformationScript> transformationScript = transformationManager.lookupScript(headersAsMap.get(Utils.PATH_HEADER_NAME));
            transformationScript.ifPresent((ts) -> {
                this.bodyScript = ts.getScript();
                builder.setModeOverride(
                        ProcessingMode.newBuilder()
                                .setRequestBodyMode(
                                        ProcessingMode.BodySendMode.BUFFERED
                                )
                                .build()
                );
            });
            return builder
                    .setRequestHeaders(
                            HeadersResponse.newBuilder()
                                    .setResponse(
                                            CommonResponse.newBuilder().build()
                                    )
                                    .build()
                    )

                    .build();
        }


        private ProcessingResponse onRequestBody(ProcessingRequest processingRequest) {
            LOG.log(Level.INFO, "On Request Body: " + processingRequest.getRequestBody().getBody().toByteArray().length + " bytes");
            ProcessingResponse.Builder builder = ProcessingResponse.newBuilder();
            if (bodyScript != null) {
                try {
                    DataWeaveResult result = bodyScript.write(new ScriptingBindings());
                    InputStream content = (InputStream) result.getContent();
                    byte[] bytes = content.readAllBytes();
                    builder.setRequestBody(
                            BodyResponse.newBuilder()
                                    .setResponse(
                                            CommonResponse.newBuilder()
                                                    .setBodyMutation(
                                                            BodyMutation.newBuilder()
                                                                    .setBody(
                                                                            ByteString.copyFrom(bytes)
                                                                    )
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return builder.build();
        }


        private ProcessingResponse onResponseTrailers(ProcessingRequest processingRequest) {
            LOG.log(Level.INFO, "On Response Trailers: " + processingRequest.getResponseTrailers().getTrailers());
            return ProcessingResponse.newBuilder().build();
        }


        //        RESPONSE FLOW
        private ProcessingResponse onResponseHeader(ProcessingRequest processingRequest) {
            LOG.log(Level.INFO, "On Response Headers: " + processingRequest.getResponseHeaders().getHeaders());
            return ProcessingResponse.newBuilder().build();
        }


        private ProcessingResponse onResponseBody(ProcessingRequest processingRequest) {
            LOG.log(Level.INFO, "On Response Body: " + processingRequest.getResponseBody().getBody().toByteArray().length + " bytes");
            return ProcessingResponse.newBuilder().build();
        }


        private ProcessingResponse onRequestTrailers(ProcessingRequest processingRequest) {
            LOG.log(Level.INFO, "On Request Trailers: " + processingRequest.getRequestTrailers().getTrailers());
            return ProcessingResponse.newBuilder().build();
        }


        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }

    }
}
