package wtf.benedict.kitchen.app;

import java.util.TimeZone;

import io.dropwizard.Application;
import io.dropwizard.bundles.webjars.WebJarBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.jbosslog.JBossLog;
import lombok.val;

@JBossLog
public class KitchenApp extends Application<KitchenConfig> {
  public static void main(String... ignored) throws Exception {
    val args = new String[] { "server", "app.yml" };
    new KitchenApp().run(args);
  }


  @Override
  public String getName() {
    return "kitchen";
  }


  @Override
  public void initialize(Bootstrap<KitchenConfig> bootstrap) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    val sourceProvider = new ResourceConfigurationSourceProvider();
    bootstrap.setConfigurationSourceProvider(sourceProvider);
    bootstrap.addBundle(new WebJarBundle());
  }


  @Override
  public void run(KitchenConfig config, Environment env) {
    log.warn("Deploying Kitchen...");
    val dependencyManager = new DependencyManager();

    env.healthChecks().register("kitchenHealthCheck", new KitchenHealthCheck());

    val jersey = env.jersey();
    jersey.register(dependencyManager.kitchenResource);

    log.warn("Deployment complete!");
  }
}
