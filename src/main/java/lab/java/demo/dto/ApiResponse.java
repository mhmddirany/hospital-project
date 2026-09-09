package lab.java.demo.dto;

/**
 * Generic API response wrapper: message + data.
 *
 * T can be any type: an entity, a list, a map, etc.
 */
public class ApiResponse<T> {

    private final String message;
    private final T data;

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
