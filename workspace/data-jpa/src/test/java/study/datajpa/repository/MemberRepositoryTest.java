package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext EntityManager em;

    @Test
    void testMember() {

        System.out.println("memberRepository = " + memberRepository.getClass());

        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void testNamedQuery() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    void testQuery() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    void findUsernameList() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void findUsernameDto() {

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    void findByNames() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void returnType() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findListByUsername("AAAAA");
        // ?????? ????????? ????????? null??? ????????? empty collection ??????
        System.out.println("result = " + result.size());

        Member findMember = memberRepository.findMemberByUsername("AAAAA");
        // ?????? ????????? ????????? null ?????? 
        System.out.println("findMember = " + findMember);
    }

    @Test
    public void paging() throws Exception {

        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        // page??? 0?????? ??????
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        // ???????????? ??????????????? ???????????? dto??? ??????
        Page<MemberDto> map = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        /**
         * Page:  ?????? count ?????? ????????? ???????????? ?????????
         * Slice: ?????? count ?????? ?????? ?????? ???????????? ?????? ??????(limit + 1 ??????)
         * List:  ?????? count ?????? ?????? ????????? ??????
         */

        // then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3); // ????????? ????????? ???
        assertThat(page.getTotalElements()).isEqualTo(5); // ?????? ????????? ???
        assertThat(page.getNumber()).isEqualTo(0); // ????????? ??????
        assertThat(page.getTotalPages()).isEqualTo(2); // ?????? ????????? ??????
        assertThat(page.isFirst()).isTrue(); // ????????? ?????????????
        assertThat(page.hasNext()).isTrue(); // ?????? ???????????? ??????????
        assertThat(page.hasPrevious()).isFalse(); // ?????? ???????????? ??????????
    }

    @Test
    void bulkUpdate() {

        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
        // ?????? ????????? DB??? ?????? ?????????????????? ????????? ????????? ?????????????????? ????????? ???????????? ??????
        // ????????? ?????? ????????? ????????? ??????????????? ???????????? ??????
        // @Modifying??? clearAutomatically ????????? ???????????? ??? ????????? ?????????
//        em.flush();
//        em.clear();

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    void findMemberLazy() {

        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when N+1
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            // member.teamClass = class study.datajpa.entity.Team$HibernateProxy$vA60yI9s
            System.out.println("member.teamClass = " + member.getTeam().getClass()); // proxy
            // tema??? name??? ???????????? ????????? ????????? ????????? ?????????
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    void findMemberFetch() {

        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findMemberFetchJoin();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            // member.teamClass = class study.datajpa.entity.Team
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

}
