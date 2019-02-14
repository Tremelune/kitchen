package wtf.benedict.box.api;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.dropwizard.testing.junit.ResourceTestRule;

public class ShelfResourceTest {
  private final ShelfResource underTest = new ShelfResource();

  @Rule
  public final ResourceTestRule rule = ResourceTestRule.builder().addResource(underTest).build();


  @Before
  public void setup() {
//    ObjectMapperConfigurer.configure(rule.getObjectMapper());
  }


  @Test
  public void getShelves() {
    Response response = rule.target("/shelves")
        .request()
        .get();

    assertEquals(200, response.getStatus());
  }
}
