package com.hobbang.account;

import com.hobbang.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {


	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AccountRepository accountRepository;

	@MockBean
	private JavaMailSender javaMailSender;

	@DisplayName("인증 메일 확인 - 입력값 오류")
	@Test
	public void checkEmailToken_with_wrong_input() throws Exception {
		mockMvc.perform(get("/check-email-token")
					.param("token", "asdfasdf")
					.param("email", "email@email.com"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("error"))
				.andExpect(view().name("account/checked-email"))
				.andExpect(unauthenticated());
	}

	@DisplayName("인증 메일 확인 - 입력값 정상")
	@Test
	public void checkEmailToken() throws Exception {
		Account account = Account.builder()
				.email("test@test.test")
				.password("12341234")
				.nickname("test")
				.build();

		Account newAccount = accountRepository.save(account);
		newAccount.generateEmailCheckToken();

		mockMvc.perform(get("/check-email-token")
					.param("token", newAccount.getEmailCheckToken())
					.param("email", newAccount.getEmail()))
				.andExpect(status().isOk())
				.andExpect(model().attributeDoesNotExist("error"))
				.andExpect(model().attributeExists("nickname"))
				.andExpect(model().attributeExists("numberOfUser"))
				.andExpect(view().name("account/checked-email"))
				.andExpect(authenticated());
	}

	@DisplayName("회원 가입 화면 보이는지 테스트")
	@Test
	void signUpForm() throws Exception {
		mockMvc.perform(get("/sign-up"))
				.andExpect(status().isOk())
				.andExpect(view().name("account/sign-up"))
				.andExpect(model().attributeExists("signUpForm"))
				.andExpect(unauthenticated());
	}

	@DisplayName("회원 가입 처리 - 입력값 오류")
	@Test
	public void signUpSubmit_with_wrong_input() throws Exception {
		mockMvc.perform(post("/sign-up")
					.param("nickname", "hobbang")
					.param("email", "unvalid..email...")
					.param("password", "12345")
					.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("account/sign-up"))
				.andExpect(unauthenticated());
	}

	@DisplayName("회원 가입 처리 - 입력값 정상")
	@Test
	public void signUpSubmit_with_correct_input() throws Exception {
		String inputPassword = "1234567890";
		String inputEmail = "hobbang@naver.com";

		mockMvc.perform(post("/sign-up")
					.param("nickname", "hobbang")
					.param("email", inputEmail)
					.param("password", inputPassword)
					.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/"))
				.andExpect(authenticated());

		Account account = accountRepository.findByEmail(inputEmail);

		assertNotNull(account);

		// 비밀번호가 암호화 되는지
		assertNotEquals(account.getPassword(), inputPassword);

		// db에 저장됬나
		assertTrue(accountRepository.existsByEmail(inputEmail));

		// email token이 저장됬나
		assertNotNull(account.getEmailCheckToken());

		// 메일 발송됬나
		then(javaMailSender).should().send(any(SimpleMailMessage.class));
	}
}