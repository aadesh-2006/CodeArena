import '@testing-library/jest-dom';

// jsdom does not implement window.scrollTo
window.scrollTo = () => {};
