// package com.wr.nutmeg.api;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.hamcrest.Matchers.notNullValue;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
// @AutoConfigureMockMvc
// @TestPropertySource(properties = {
//         "nutmeg.seed.enabled=true",
//         "nutmeg.seed.club-count=8",
//         "nutmeg.seed.admin-password=admin123"
// })
// class AuthControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Test
//     void loginWithUsernameReturnsJwt() throws Exception {
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("""
//                                 {"login":"league-admin","password":"admin123"}
//                                 """))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.accessToken").value(notNullValue()))
//                 .andExpect(jsonPath("$.tokenType").value("Bearer"))
//                 .andExpect(jsonPath("$.manager.username").value("league-admin"));
//     }

//     @Test
//     void loginWithEmailReturnsJwt() throws Exception {
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("""
//                                 {"login":"admin@nutmeg.local","password":"admin123"}
//                                 """))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.accessToken").value(notNullValue()))
//                 .andExpect(jsonPath("$.manager.email").value("admin@nutmeg.local"));
//     }

//     @Test
//     void rejectsInvalidCredentials() throws Exception {
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("""
//                                 {"login":"league-admin","password":"wrong-password"}
//                                 """))
//                 .andExpect(status().isUnauthorized());
//     }

//     @Test
//     void meRequiresAuthentication() throws Exception {
//         mockMvc.perform(get("/api/auth/me"))
//                 .andExpect(status().isUnauthorized());
//     }

//     @Test
//     void meReturnsCurrentManagerWithValidToken() throws Exception {
//         String response = mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("""
//                                 {"login":"league-admin","password":"admin123"}
//                                 """))
//                 .andExpect(status().isOk())
//                 .andReturn()
//                 .getResponse()
//                 .getContentAsString();

//         String token = response.split("\"accessToken\":\"")[1].split("\"")[0];

//         mockMvc.perform(get("/api/auth/me")
//                         .header("Authorization", "Bearer " + token))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.username").value("league-admin"))
//                 .andExpect(header().doesNotExist("Set-Cookie"));
//     }
// }
