package es.neesis.demospringbatch.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserEntity {
    private String id;
    private String username;
    private String password;
    private String email;
    private String operation;
}
