package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    // EnumType.ORDINAL 은 숫자로 들어가게 되는데
    // 이넘 타입이 추가되면 숫자가 밀리게 되어 정보들이 섞인다
    // 따라서 문자열로 해주어야 안전하다

}
