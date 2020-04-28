package com.hobbang.settings;

import com.hobbang.WithAccount;
import com.hobbang.account.AccountRepository;
import com.hobbang.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;


    @AfterEach
    void AfterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("hobbang")
    @DisplayName("프로필 수정 폼")
    @Test
    public void updateProfileForm() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

        Account hobbang = accountRepository.findByNickname("hobbang");
        assertEquals(bio, hobbang.getBio());
    }
    @WithAccount("hobbang")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    public void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));


        Account hobbang = accountRepository.findByNickname("hobbang");
        assertEquals(bio, hobbang.getBio());
    }
    @WithAccount("hobbang")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    public void updateProfile_with_wrong() throws Exception {
        String bio = "길게 소개를 수정하는 경우,길게 소개를 수정하는 경우,길게 소개를 수정하는 경우,길게 소개를 수정하는 경우,길게 소개를 수정하는 경우";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());


        Account hobbang = accountRepository.findByNickname("hobbang");
        assertNull(hobbang.getBio());
    }
}