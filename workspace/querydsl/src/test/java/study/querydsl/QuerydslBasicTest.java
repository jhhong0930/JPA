package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {

        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {

        // member1을 찾아라
        String qlString = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {

        // member1을 찾아라

//        QMember m = new QMember("m"); // 별칭 직접 지정
//        QMember qMember = QMember.member; // 기본 인스턴스 사용

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {

        Member findMember = queryFactory
                .selectFrom(member)
//                .where(member.username.eq("member1")
//                        .and(member.age.eq(10)))
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        /*
        member.username.eq("member1")        // username = 'member1'
        member.username.ne("member1")        // username != 'member1'
        member.username.eq("member1").not()  // username != 'member1'
        member.username.isNotNull()          // 이름이 is not null
        member.age.in(10, 20)                // age in (10,20)
        member.age.notIn(10, 20)             // age not in (10, 20)
        member.age.between(10,30)            // between 10, 30
        member.age.goe(30)                   // age >= 30
        member.age.gt(30)                    // age > 30
        member.age.loe(30)                   // age <= 30
        member.age.lt(30)                    // age < 30
        member.username.like("member%")      // like 검색
        member.username.contains("member")   // like ‘%member%’ 검색
        member.username.startsWith("member") // like ‘member%’ 검색
         */

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}