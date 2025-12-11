package com.stitcho.beta.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.util.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String googleId = oAuth2User.getAttribute("sub");

        User user = userRepository.findByEmail(email);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            user.setProfilePicture(picture);
            user.setProvider("google");
            user.setPassword("");

            Role customerRole = roleRepository.findByRoleName("customer");
            if (customerRole == null) {
                customerRole = new Role();
                customerRole.setRoleName("customer");
                customerRole = roleRepository.save(customerRole);
            }
            user.setRole(customerRole);
            user = userRepository.save(user);
        } else {
            user.setGoogleId(googleId);
            user.setProfilePicture(picture);
            user.setProvider("google");
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getName(), user.getRole().getRoleName());

        String redirectUrl = "http://localhost:3000/oauth-success?token=" + token + "&email=" + user.getEmail() + "&name=" + user.getName()
                + "&role=" + user.getRole().getRoleName();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
