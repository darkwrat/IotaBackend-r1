package ru.cdecl.pub.iota.tests;

import ru.cdecl.pub.iota.exceptions.UserAlreadyExistsException;
import ru.cdecl.pub.iota.models.UserProfile;
import ru.cdecl.pub.iota.services.AccountService;
import java.lang.NullPointerException;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class AccountServiceImplTest {
    @Inject
    AccountService accountService;

    @Test
    public void testCreateUser() throws UserAlreadyExistsException, NullPointerException {
        final UserProfile userProfile = new UserProfile(1L, "vasya", "vasya@mail.ru");
        accountService.createUser(userProfile, "password".toCharArray());

        assertEquals(1L, accountService.getUserId("vasya").longValue());
    }

    @Test
    public void testEditUser() throws Exception {
        final UserProfile userProfile = new UserProfile(1L, "Vasya", "vasya@mail.ru");
        accountService.createUser(userProfile, "password".toCharArray());

        final UserProfile newUserProfile = new UserProfile(1L, "Vasya", "Vasiliy@mail.ru");
        accountService.editUser(1L, newUserProfile, "password".toCharArray());

        final String resultEmail = accountService.getUserProfile(1L).getEmail();

        assertEquals("Vasiliy@mail.ru", resultEmail);
    }

    @Test
    public void testDeleteUser() throws Exception {
        final UserProfile userProfile = new UserProfile(1L, "vasya", "vasya@mail.ru");

        accountService.createUser(userProfile, "password".toCharArray());
        accountService.deleteUser(1L);

        assertFalse(accountService.isUserExistent(1L));
    }

    @Test
    public void testGetUserId() throws Exception {

    }

    @Test
    public void testGetUserProfile() throws Exception {

    }

    @Test
    public void testIsUserPasswordCorrect() throws Exception {

    }

    @Test
    public void testIsUserExistent() throws Exception {

    }
}
