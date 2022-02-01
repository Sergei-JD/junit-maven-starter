package com.junit.starter.service;

import com.junit.starter.TestBase;
import com.junit.starter.dao.UserDao;
import com.junit.starter.dto.User;
import com.junit.starter.extension.*;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("fast")
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
//        ThrowableExtension.class
//        GlobalExtension.class
})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest extends TestBase  {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;

    @Mock(lenient = true)
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
//        lenient().when(userDao.delete(IVAN.getId())).thenReturn(true);
//        Mockito.mockStatic()

        doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.mock(UserDao.class, withSettings().lenient());
//        this.userDao = Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        //given when
        doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());

        // then
        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        // given
        userService.add(IVAN);
//        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());

//        Mockito.when(userDao.delete(IVAN.getId()))
//                .thenReturn(true)
//                .thenReturn(false);

        // -> - ->
//        BDDMockito.given(userDao.delete(IVAN.getId())).willReturn(true);
        // <- - ->
//        BDDMockito.willReturn(true).given(userDao).delete(IVAN.getId());

        // when
        var deleteResult = userService.delete(IVAN.getId());
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

        // then
//        var argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(userDao, times(3)).delete(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());

//        Mockito.reset(userDao);

        assertThat(deleteResult).isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("users will be empty if no user added")
    void usersEmptyIfNoUserAdded() throws IOException {
        // given when
        if (true) {
            throw new RuntimeException();
        }
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        // then
            // hamcrest
        MatcherAssert.assertThat(users, empty());

            // assertj
        assertTrue(users.isEmpty(), () -> "User list should be empty");
            // input ->[box == func] -> actual output
    }

    @Test
    @Order(2)
    void usersSizeIfUserAdded() {
        // given
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        // when
        var users = userService.getAll();

        // then
        assertThat(users).hasSize(2);
//        assertEquals(2, users.size());
    }

    @Test
    void usersConvertedToMapById() {
        // given
        userService.add(IVAN, PETR);

        // when
        Map<Integer, User> users = userService.getAllConvertedById();

        // then
            // hamcrest
        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId()));

            // assertj
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }

    @Nested
    @Tag("login")
    @DisplayName("test user login functionality")
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    class LoginTest {

        @Test
        @Disabled("flaky, need to see")
        void loginFailIfPasswordIsNotCorrect() {
            // given
            userService.add(IVAN);

            // when
            var maybeUser = userService.login(IVAN.getUsername(), "dummy");

            // then
            assertTrue(maybeUser.isEmpty());
        }

        @Test
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfUserDoesNotExist() {
            // given
            userService.add(IVAN);

            // when
            var maybeUser = userService.login("dummy", IVAN.getPassword());

            // then
            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void checkLoginFunctionalityPerformance() {
            // given
            System.out.println(Thread.currentThread().getName());

            // when then
            var result = assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(100L);

                return userService.login("dummy", IVAN.getPassword());
            });
        }

        @Test
        void loginSuccessIfUserExists() {
            // given
            userService.add(IVAN);

            // when
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            // then
                // assertj
            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));

            // junit standart
//        assertTrue(maybeUser.isPresent());
//        maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        }

        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
            // given when then
            assertAll(
                    () -> {
                        var exception = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @ParameterizedTest(name = "{arguments} test")
//        @ArgumentsSource()
//        @NullSource
//        @EmptySource
//        @ValueSource(strings = {
//                "Ivan", "Petr"
//        })
//        @NullAndEmptySource
//        @EnumSource
        @MethodSource("com.junit.starter.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({
//                "Ivan,123",
//                "Petr,111"
//        })
        @DisplayName("login param test")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            // given
            userService.add(IVAN, PETR);

            // when
            var maybeUser = userService.login(username, password);

            // then
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
