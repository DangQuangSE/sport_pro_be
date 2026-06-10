package com.sport_pro_be.modules.size.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "size_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeGroup extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "sizeGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    @OrderBy("displayOrder ASC")
    private List<SizeOption> sizes = new ArrayList<>();
}
