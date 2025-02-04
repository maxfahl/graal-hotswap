async function sayHelloSoon() {
    await new Promise(resolve => setTimeout(resolve, 1000));
    console.log('Hello from JavaScript!');
    return 'Hello from JavaScript!';
} 