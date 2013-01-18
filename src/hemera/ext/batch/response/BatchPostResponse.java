package hemera.ext.batch.response;

import hemera.core.structure.AbstractResponse;
import hemera.core.structure.enumn.EHttpStatus;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <code>BatchPostResponse</code> defines the response
 * for batch resource post operation that returns an
 * array of JSON formatted responses.
 *
 * @author Yi Wang (Neakor)
 * @version 1.0.0
 */
public class BatchPostResponse extends AbstractResponse {
	/**
	 * The array of <code>JSONObject</code> responses.
	 */
	private final JSONObject[] responses;
	
	/**
	 * Constructor of <code>BatchPostResponse</code>.
	 * @param responses The array of <code>JSONObject</code>
	 * responses.
	 */
	public BatchPostResponse(final JSONObject[] responses) {
		this.responses = responses;
	}

	/**
	 * Constructor of <code>BatchPostResponse</code>.
	 * @param status The error <code>EHttpStatus</code>.
	 * @param error The <code>String</code> error message.
	 */
	public BatchPostResponse(final EHttpStatus status, final String error) {
		super(status, error);
		this.responses = null;
	}
	
	@Override
	protected void insertData(final JSONObject data) throws Exception {
		if (this.responses != null) {
			final JSONArray array = new JSONArray();
			for (int i = 0; i < this.responses.length; i++) {
				array.put(this.responses[i]);
			}
			data.put("responses", array);
		}
	}
}
