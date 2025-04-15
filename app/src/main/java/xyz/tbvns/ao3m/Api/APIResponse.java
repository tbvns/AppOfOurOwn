package xyz.tbvns.ao3m.Api;

import androidx.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse<E> {
    private boolean success;
    @Nullable
    private String message;
    @Nullable
    private E object;
}
