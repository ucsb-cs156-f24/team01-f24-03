package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

        @MockBean
        ArticlesRepository ArticlesRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdates/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().is(200)); // logged
        }

        // Authorization tests for /api/articles/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/articles/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/articles/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_articles() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Articles articles1 = Articles.builder()
                                .title("Articles1") // Updated from "Article1"
                                .url("url1")
                                .explanation("explanation1")
                                .email("email1")    
                                .dateadded(ldt1)
                                .build();


                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                Articles articles2 = Articles.builder()
                                .title("Articles2")
                                .url("url2")
                                .explanation("explanation2")
                                .email("email2")    
                                .dateadded(ldt2)
                                .build();

                ArrayList<Articles> expectedDates = new ArrayList<>();
                expectedDates.addAll(Arrays.asList(articles1, articles2));

                when(ArticlesRepository.findAll()).thenReturn(expectedDates);

                // act
                MvcResult response = mockMvc.perform(get("/api/articles/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ArticlesRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedDates);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_articles() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Articles articles1 = Articles.builder()
                                .title("Articles1")
                                .url("url1")
                                .explanation("explanation1")
                                .email("email1")    
                                .dateadded(ldt1)
                                .build();

                when(ArticlesRepository.save(eq(articles1))).thenReturn(articles1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/articles/post?title=Articles1&url=url1&explanation=explanation1&email=email1&dateadded=2022-01-03T00:00:00")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ArticlesRepository, times(1)).save(articles1);
                String expectedJson = mapper.writeValueAsString(articles1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
}
