package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PeepLocation {
    @Id
    @Column(name = "peep_id")
    private Long id; // 핍 고유 ID (외래키)

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn
    private Peep peep; // 핍 고유 ID

    @Column(nullable = false, name = "latitude")
    private Double latitude; // 위도

    @Column(nullable = false, name = "longitude")
    private Double longitude; // 경도

    @Column(nullable = false, name = "postal_code")
    private String postalCode; // 우편번호

    @Column(nullable = false, name = "road_name_address")
    private String roadNameAddress; // 전체 도로명 주소

    @Column(nullable = false, name = "road_name_code")
    private String roadNameCode; // 도로명 코드

    @Column(nullable = false)
    private String building; // 건물 이름

    /*
    신규 PeepLocation 생성
     */
    @Builder
    public PeepLocation(Peep peep, Double latitude, Double longitude,String postalCode,
                        String roadNameAddress, String roadNameCode,  String building) {
        this.peep = peep;
        this.latitude = latitude;
        this.longitude = longitude;
        this.postalCode = postalCode;
        this.roadNameAddress = roadNameAddress;
        this.roadNameCode = roadNameCode;
        this.building = building;
    }
}
