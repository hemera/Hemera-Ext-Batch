package hemera.ext.batch.processor;

import org.json.JSONObject;

import hemera.core.execution.interfaces.IExecutionService;
import hemera.core.execution.interfaces.task.IResultTask;
import hemera.core.execution.interfaces.task.handle.IResultTaskHandle;
import hemera.core.structure.enumn.EHttpStatus;
import hemera.ext.batch.request.BatchPostRequest;
import hemera.ext.batch.response.BatchPostResponse;
import hemera.ext.batch.util.Request;
import hemera.ext.batch.util.RequestSender;
import hemera.ext.oauth.processor.AbstractOAuthProcessor;
import hemera.ext.oauth.token.AbstractAccessToken;

/**
 * <code>AbstractBatchPostProcessor</code> defines the
 * processor for batch resource post operation that
 * performs an array of requests at once.
 *
 * @author Yi Wang (Neakor)
 * @version 1.0.1
 */
public abstract class AbstractBatchPostProcessor<RQ extends BatchPostRequest> extends AbstractOAuthProcessor<RQ, BatchPostResponse> {
	/**
	 * The <code>IExecutionService</code> instance.
	 */
	private final IExecutionService execution;
	/**
	 * The <code>RequestSender</code> instance.
	 */
	private final RequestSender sender;
	
	/**
	 * Constructor of <code>PBatchPost</code>.
	 * @param execution The <code>IExecutionService</code>
	 * instance.
	 */
	public AbstractBatchPostProcessor(final IExecutionService execution, final String baseURL) {
		this.execution = execution;
		this.sender = new RequestSender(baseURL);
	}

	@Override
	protected BatchPostResponse unauthorizedResponse(final RQ request) throws Exception {
		return new BatchPostResponse(EHttpStatus.C401_Unauthorized, "Unauthorized request.");
	}

	@Override
	protected BatchPostResponse processAuthorizedRequest(final AbstractAccessToken accessToken, final RQ request) throws Exception {
		// Send all tasks in parallel.
		@SuppressWarnings("unchecked")
		final IResultTaskHandle<JSONObject>[] handles = new IResultTaskHandle[request.requests.length];
		for (int i = 0; i < request.requests.length; i++) {
			final RequestTask task = new RequestTask(request.requests[i]);
			handles[i] = this.execution.submit(task);
		}
		// Wait for all responses.
		final JSONObject[] responses = new JSONObject[handles.length];
		for (int i = 0; i < handles.length; i++) {
			final JSONObject response = handles[i].getAndWait();
			responses[i] = response;
		}
		return new BatchPostResponse(responses);
	}

	@Override
	protected BatchPostResponse exceptionResponse(final RQ request, final Exception e) {
		return new BatchPostResponse(EHttpStatus.C500_InternalServerError, e.getMessage());
	}
	
	/**
	 * <code>RequestTask</code> defines the result task
	 * that sends a request and waits for its response.
	 *
	 * @author Yi Wang (Neakor)
	 * @version 1.0.0
	 */
	private class RequestTask implements IResultTask<JSONObject> {
		/**
		 * The <code>Request</code> instance.
		 */
		private final Request request;
		
		/**
		 * Constructor of <code>RequestTask</code>.
		 * @param request The <code>Request</code> instance.
		 */
		private RequestTask(final Request request) {
			this.request = request;
		}

		@Override
		public JSONObject execute() throws Exception {
			try {
				// Complete request dependencies.
				final JSONObject failedChildResponse = this.request.completeDependencies(AbstractBatchPostProcessor.this.sender);
				// If dependency failed, just returned failed response.
				if (failedChildResponse != null) return failedChildResponse;
				// Otherwise, send request.
				return AbstractBatchPostProcessor.this.sender.sendRequest(this.request);
			} catch (final Exception e) {
				return new JSONObject().put("exception", e.getMessage());
			}
		}
	}
}
