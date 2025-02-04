console.log("Script loaded and ready!");

function sayHelloSoon() {
  return new Promise((resolve) => {
    console.log("I'm a bit lazy, it'll take 2 seconds to say hello");
    setTimeout(() => {
      resolve("Hello from JavaScript!");
    }, 2000);
  });
}
