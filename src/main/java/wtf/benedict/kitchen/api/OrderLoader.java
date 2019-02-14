package wtf.benedict.kitchen.api;

import static com.google.gson.stream.JsonToken.NULL;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import lombok.AllArgsConstructor;
import lombok.val;
import wtf.benedict.kitchen.biz.Order;
import wtf.benedict.kitchen.biz.Order.Temperature;
import wtf.benedict.kitchen.biz.OrderIdGenerator;

@AllArgsConstructor
class OrderLoader {
  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(Temperature.class, new TemperatureAdapter())
      .create();

  private final Iterator<Order> orders = loadOrders();

  private final Clock clock;
  private final OrderIdGenerator orderIdGenerator;


  Order next() {
    val order = orders.next();
    order.setId(orderIdGenerator.next());
    order.setReceived(clock.instant());
    return order;
  }


  private Iterator<Order> loadOrders() {
    val orders = gson.fromJson(ORDERS, Order[].class);
    return Arrays.asList(orders).iterator();
  }


  // Java makes it hard to use enum values for serialization, and easy to use the fragile enum
  // names, so we jump a hoop:
  //
  // https://stackoverflow.com/questions/8863429/how-to-handle-a-numberformatexception-with-gson-in-deserialization-a-json-respon
  private static class TemperatureAdapter extends TypeAdapter<Temperature> {
    @Override
    public Temperature read(JsonReader reader) throws IOException {
      if (reader.peek() == NULL) {
        reader.nextNull();
        return null;
      }

      return Temperature.fromValue(reader.nextString());
    }

    // We don't ever write.
    @Override
    public void write(JsonWriter writer, Temperature value) {
      System.out.println();
    }
  }



  // Ignore me doing this...
  private static final String ORDERS = "[\n" +
      "  {\n" +
      "    \"name\": \"Banana Split\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 20,\n" +
      "    \"decayRate\": 0.63\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"McFlury\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 375,\n" +
      "    \"decayRate\": 0.4\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Acai Bowl\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 249,\n" +
      "    \"decayRate\": 0.3\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Yogurt\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 263,\n" +
      "    \"decayRate\": 0.37\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chocolate Gelato\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 300,\n" +
      "    \"decayRate\": 0.61\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cobb Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 269,\n" +
      "    \"decayRate\": 0.19\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cottage Cheese\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 251,\n" +
      "    \"decayRate\": 0.22\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Coke\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 240,\n" +
      "    \"decayRate\": 0.25\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Snow Cone\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 50,\n" +
      "    \"decayRate\": 0.86\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pad See Ew\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 210,\n" +
      "    \"decayRate\": 0.72\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chunky Monkey\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 210,\n" +
      "    \"decayRate\": 0.54\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Beef Stew\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 206,\n" +
      "    \"decayRate\": 0.69\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cheese\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 255,\n" +
      "    \"decayRate\": 0.2\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Spinach Omelet\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 230,\n" +
      "    \"decayRate\": 0.63\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Beef Hash\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 30,\n" +
      "    \"decayRate\": 0.74\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pork Chop\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Kale Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 250,\n" +
      "    \"decayRate\": 0.25\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fresh Fruit\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 252,\n" +
      "    \"decayRate\": 0.29\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cranberry Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 245,\n" +
      "    \"decayRate\": 0.21\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fudge Ice Cream Cake\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 415,\n" +
      "    \"decayRate\": 0.49\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Mint Chocolate Ice Cream\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 290,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Vegan Pizza\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Orange Chicken\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 215,\n" +
      "    \"decayRate\": 0.67\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"MeatLoaf\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 213,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Milk\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 252,\n" +
      "    \"decayRate\": 0.15\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pastrami Sandwich\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 190,\n" +
      "    \"decayRate\": 0.8\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Arugula\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 251,\n" +
      "    \"decayRate\": 0.27\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pickles\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 259,\n" +
      "    \"decayRate\": 0.29\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chicken\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 201,\n" +
      "    \"decayRate\": 0.74\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cookie Dough\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 600,\n" +
      "    \"decayRate\": 0.15\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Hamburger\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.63\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"French Fries\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 220,\n" +
      "    \"decayRate\": 0.67\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Ice\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 100,\n" +
      "    \"decayRate\": 0.9\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Carne Asada\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 222,\n" +
      "    \"decayRate\": 0.71\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Sherbet\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 175,\n" +
      "    \"decayRate\": 0.6\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Orange Sorbet\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 165,\n" +
      "    \"decayRate\": 0.65\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Frosty\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 135,\n" +
      "    \"decayRate\": 0.52\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fresh Bread\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 201,\n" +
      "    \"decayRate\": 0.9\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Burrito\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 202,\n" +
      "    \"decayRate\": 0.72\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Icy\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 230,\n" +
      "    \"decayRate\": 0.6\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Push Pop\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 220,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pasta\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chicken Nuggets\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 205,\n" +
      "    \"decayRate\": 0.71\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Ice Cream Sandwich\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 250,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Taco\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 198,\n" +
      "    \"decayRate\": 0.38\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Tomato Soup\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 243,\n" +
      "    \"decayRate\": 0.71\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Vanilla Ice Cream\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 310,\n" +
      "    \"decayRate\": 0.35\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Poppers\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 204,\n" +
      "    \"decayRate\": 0.78\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Popsicle\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 345,\n" +
      "    \"decayRate\": 0.75\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Strawberries\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 500,\n" +
      "    \"decayRate\": 0.05\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Brown Rice\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 224,\n" +
      "    \"decayRate\": 0.64\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cheese Pizza\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.76\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pressed Juice\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 250,\n" +
      "    \"decayRate\": 0.2\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Coconut\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 254,\n" +
      "    \"decayRate\": 0.22\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Onion Rings\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 201,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fish Tacos\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 207,\n" +
      "    \"decayRate\": 0.74\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pot Stickers\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 204,\n" +
      "    \"decayRate\": 0.73\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Kombucha\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 246,\n" +
      "    \"decayRate\": 0.19\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Mixed Greens\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 252,\n" +
      "    \"decayRate\": 0.26\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Sushi\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 251,\n" +
      "    \"decayRate\": 0.25\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Apples\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 244,\n" +
      "    \"decayRate\": 0.23\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Kebab\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.54\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Mac & Cheese\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 205,\n" +
      "    \"decayRate\": 0.51\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Corn Dog\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 203,\n" +
      "    \"decayRate\": 0.3\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Grilled Corn Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 305,\n" +
      "    \"decayRate\": 0.1\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pistachio Ice Cream\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 175,\n" +
      "    \"decayRate\": 0.4\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Strawberyy Banana Split\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 24,\n" +
      "    \"decayRate\": 0.60\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"McFlury\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 372,\n" +
      "    \"decayRate\": 0.45\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Acai Bowl\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 240,\n" +
      "    \"decayRate\": 0.9\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Yogurt\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 400,\n" +
      "    \"decayRate\": 0.67\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chocolate Gelato\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 308,\n" +
      "    \"decayRate\": 0.67\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cobb Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 263,\n" +
      "    \"decayRate\": 0.2\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cottage Cheese\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 255,\n" +
      "    \"decayRate\": 0.26\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Coke\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 240,\n" +
      "    \"decayRate\": 0.25\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Snow Cone\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 50,\n" +
      "    \"decayRate\": 0.86\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pad See Ew\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 210,\n" +
      "    \"decayRate\": 0.72\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chunky Monkey\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 210,\n" +
      "    \"decayRate\": 0.54\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Beef Stew\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 206,\n" +
      "    \"decayRate\": 0.69\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cheese\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 255,\n" +
      "    \"decayRate\": 0.2\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Spinach Omelet\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 230,\n" +
      "    \"decayRate\": 0.63\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Beef Hash\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 30,\n" +
      "    \"decayRate\": 0.74\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pork Chop\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Kale Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 250,\n" +
      "    \"decayRate\": 0.25\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fresh Fruit\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 252,\n" +
      "    \"decayRate\": 0.29\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cranberry Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 245,\n" +
      "    \"decayRate\": 0.21\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fudge Ice Cream Cake\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 415,\n" +
      "    \"decayRate\": 0.49\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Mint Chocolate Ice Cream\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 290,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Vegan Pizza\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Orange Chicken\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 215,\n" +
      "    \"decayRate\": 0.67\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"MeatLoaf\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 213,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Milk\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 252,\n" +
      "    \"decayRate\": 0.15\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pastrami Sandwich\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 190,\n" +
      "    \"decayRate\": 0.8\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Arugula\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 251,\n" +
      "    \"decayRate\": 0.27\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pickles\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 259,\n" +
      "    \"decayRate\": 0.29\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chicken\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 201,\n" +
      "    \"decayRate\": 0.74\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cookie Dough\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 600,\n" +
      "    \"decayRate\": 0.15\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Hamburger\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.63\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"French Fries\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 220,\n" +
      "    \"decayRate\": 0.67\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Ice\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 100,\n" +
      "    \"decayRate\": 0.9\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Carne Asada\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 222,\n" +
      "    \"decayRate\": 0.71\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Sherbet\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 175,\n" +
      "    \"decayRate\": 0.6\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Orange Sorbet\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 165,\n" +
      "    \"decayRate\": 0.65\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Frosty\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 135,\n" +
      "    \"decayRate\": 0.52\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fresh Bread\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 201,\n" +
      "    \"decayRate\": 0.9\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Burrito\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 202,\n" +
      "    \"decayRate\": 0.72\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Icy\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 230,\n" +
      "    \"decayRate\": 0.6\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Push Pop\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 220,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pasta\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Chicken Nuggets\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 205,\n" +
      "    \"decayRate\": 0.71\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Ice Cream Sandwich\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 250,\n" +
      "    \"decayRate\": 0.5\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Taco\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 198,\n" +
      "    \"decayRate\": 0.38\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Tomato Soup\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 243,\n" +
      "    \"decayRate\": 0.71\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Vanilla Ice Cream\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 310,\n" +
      "    \"decayRate\": 0.35\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Poppers\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 204,\n" +
      "    \"decayRate\": 0.78\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Popsicle\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 345,\n" +
      "    \"decayRate\": 0.75\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Strawberries\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 500,\n" +
      "    \"decayRate\": 0.05\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Brown Rice\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 224,\n" +
      "    \"decayRate\": 0.64\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Cheese Pizza\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.76\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pressed Juice\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 250,\n" +
      "    \"decayRate\": 0.2\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Coconut\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 254,\n" +
      "    \"decayRate\": 0.22\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Onion Rings\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 201,\n" +
      "    \"decayRate\": 0.7\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Fish Tacos\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 207,\n" +
      "    \"decayRate\": 0.74\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pot Stickers\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 204,\n" +
      "    \"decayRate\": 0.73\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Kombucha\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 246,\n" +
      "    \"decayRate\": 0.19\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Mixed Greens\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 252,\n" +
      "    \"decayRate\": 0.26\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Sushi\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 251,\n" +
      "    \"decayRate\": 0.25\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Apples\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 244,\n" +
      "    \"decayRate\": 0.23\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Kebab\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 200,\n" +
      "    \"decayRate\": 0.54\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Mac & Cheese\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 205,\n" +
      "    \"decayRate\": 0.51\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Corn Dog\",\n" +
      "    \"temp\": \"hot\",\n" +
      "    \"shelfLife\": 203,\n" +
      "    \"decayRate\": 0.3\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Grilled Corn Salad\",\n" +
      "    \"temp\": \"cold\",\n" +
      "    \"shelfLife\": 305,\n" +
      "    \"decayRate\": 0.1\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Pistachio Ice Cream\",\n" +
      "    \"temp\": \"frozen\",\n" +
      "    \"shelfLife\": 175,\n" +
      "    \"decayRate\": 0.4\n" +
      "  }\n" +
      "]\n";
}
