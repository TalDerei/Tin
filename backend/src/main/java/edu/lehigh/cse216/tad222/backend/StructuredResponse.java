package edu.lehigh.cse216.tad222.backend;
import spark.Response;

/**
 * StructuredResponse provides a common format for success and failure messages,
 * with an optional payload of type Object that can be converted into JSON.
 * 
 * NB: since this will be converted into JSON, all fields must be public.
 */
public class StructuredResponse {
    /**
     * The status is a string that the application can use to quickly determine
     * if the response indicates an error.  Values will probably just be "ok" or
     * "error", but that may evolve over time.
     */
    public String mStatus;

    /**
     * The message is only useful when this is an error, or when data is null.
     */
    public String mMessage;

    /**
     * Any JSON-friendly object can be referenced here, so that we can have a
     * rich reply to the client
     */
    public Object mData;

    /**
     * Construct a StructuredResponse by providing a status, message, and data.
     * If the status is not provided, set it to "invalid".
     * 
     * @param status The status of the response, typically "ok" or "error"
     * @param message The message to go along with an error status
     * @param object An object with additional data to send to the client
     */
    public StructuredResponse(String status, String message, Object data) {
        mStatus = (status != null) ? status : "invalid";
        mMessage = message;
        mData = data;
    }

    public StructuredResponse(Response response) {
        this("error", "Unable to authorize with provided token. Ensure token is still in memory and not null", null);
        response.type("application/json");
        response.status(200);
    }
}