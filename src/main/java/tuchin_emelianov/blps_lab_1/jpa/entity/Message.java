package tuchin_emelianov.blps_lab_1.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String object;

    @JoinColumn
    @ManyToOne
    private Human receiver;

    @Column
    private String content;
}
