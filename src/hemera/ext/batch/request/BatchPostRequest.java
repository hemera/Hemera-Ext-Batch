package hemera.ext.batch.request;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import hemera.ext.batch.util.Request;
import hemera.ext.oauth.request.AbstractOAuthRequest;

/**
 * <code>BatchPostRequest</code> defines the request for
 * the batch resource post operation that accepts an
 * array of requests.
 *
 * @author Yi Wang (Neakor)
 * @version 1.0.0
 */
public class BatchPostRequest extends AbstractOAuthRequest {
	/**
	 * The array of <code>Request</code> to be sent.
	 */
	public Request[] requests;

	@Override
	public void parse(final String[] path, final Map<String, Object> arguments) throws Exception {
		super.parse(path, arguments);
		final String batchStr = (String)arguments.get("requests");
		if (batchStr == null || batchStr.trim().isEmpty()) {
			throw new IllegalArgumentException("Requests must be specified.");
		}
		final JSONArray array = new JSONArray(batchStr);
		final int count = array.length();
		this.requests = new Request[count];
		for (int i = 0; i < count; i++) {
			final JSONObject data = array.getJSONObject(i);
			this.requests[i] = new Request(data);
		}
	}
}
