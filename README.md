# Benedict Cajun Kitchen

A wild kitchen delivery service for getting that bread; getting that chedder.

## Running

The easiest way to get the code is probably to checkout the repo from GitHub:

https://github.com/Tremelune/kitchen.git

Then, to build:

`$ mvn clean package`

Then, to run:

`$ java -jar target/kitchen-0.0.1-SNAPSHOT.jar`

Then, point a web browser to [localhost:8080](http://localhost.com:8080/) and press the "Generate
 orders" button.

#### Speed

If you want to slow things down to get a better feel for what's going on, you can change the rate
of pickup in `DriverDepot.getPickupDelay()` and/or the rate of order generation in `OrderGenerator`.

## Features

• The decay strategy is contained in a class behind an interface such that it can be simply replaced
on a per-order basis.

• Depletion from decay is tracked over time for each shelf the order spends time on. If it is moved to the overflow
shelf and then back to the frozen shelf, the depletion for the accelrated decay on the overflow shelf is
taken into account when determing the remaining shelf life ("normalized value").

• If an order times out or gets pushed off all shelves, it is stored in a trash that is displayed.

• When an order is trashed, the delivery driver for that order is canceled.

• Delivery drivers are tracked by time-until-pickup.

#### Overflow Strategy

The overflow strategy is to try and maximize the freshness of orders and the number of orders that don't expire
on the shelf. If the hot shelf is full and we place a new hot order, the order that will live the longest
on the overflow shelf is the one that is put there. Decay rate and shelf life are both taken into account
such that an order with a decay rate of .1 and a shelf life of 20 will go to the overflow before an order
with a decay rate of 10 and a shelf life of 100.

Orders are eagerly pulled from the overflow shelf when space frees up on another shelf.

The theory is that by keeping the longest-living orders on the overflow, there will be fewer overall
trashed orders. It may be that coordination between driver pickup time and shelf life is the better way
to go, but without tracking metrics, we'll never know for sure...

## Architecture

I started out invisioning a CRUD app with datastores and caches and what not, and so the basic framework
is that of a typical DropWizard app. I'm not advocating it as my top choice for a service, but it's what I'm
actively working with, so it's most familiar. Indeed, I wouldn't even recommend Java for one-off
projects typically, but it's my bread and butter and I didn't want to fight the tools.

I never got around to building an event-sourced system or distributed cache, and there's no persistence layer
to speak of, so this doesn't demonstrate that knowledge, unfortunately. Everything is pretty much
in-memory, which is an unusual app for me to be working on. I'm not a fast prototype builder, but my
 systems have gone five years (and counting?) without rewrites.

## Design

The most important aspect of designing a self-contained app or service is the separation of concerns. The API
stuff that handles URL routing, parsing requests, and structuring responses should not be intermingled with
the core business logic. Both of those are complex enough that the complexity is somewhat cartesian when
they're mixed together.

Similarly, you don't want the logic that controls how your state is stored (database or memory) to be
intermingled with your business logic. DAOs and clients to 3rd party apps shouldn't know about each other
and they shouldn't know how to figure out if an account is active or expired or what.

For this app, that last bit is kinda loose. Since there's no persistence layer, it seemed overly complex
to break maps and sets out into their own tier with DAOs and what not, so they're a bit mixed in with
logic relevant to them. I tried to err on the side of a self-contained DDD aggregate that has control over its
own data. Some things were exploded a bit for serialization, as this seemed like a pragmatic approach
for display.

#### Coupling

After having built this thing, I'm seeing that there is more coupling of the objects that store
state than I'd like—the temperature shelves know about the overflow shelf, and everybody knows about the trash. There wound
up being some pretty deep injection of listeners to coordinate what should happen when an order is placed or is "dead"
(such as dispatching drivers, canceling drivers, re-balancing the overflow shelf when room frees up on 
the cold shelf, dropping orders in the trash when they expire, etc, etc).

In retrospect, this probably would have been more decoupled to have built it on an event bus. Shout
out to the year I spent building Android apps for Squarespace.

#### Time

Time is always tricky. Wherever possible, I tried to pass a Clock around so I could control time in tests,
but enough of the system relied on system time (Timer, and the ExpiringMap) that there are still some pain
points there. I didn't want to try and unpack/tweak/rewrite these classes...

## License

Whatever, man. Do as thou wilt.