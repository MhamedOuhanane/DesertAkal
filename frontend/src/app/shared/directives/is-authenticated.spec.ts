import { IsAuthenticated } from './is-authenticated';

describe('IsAuthenticated', () => {
  it('should create an instance', () => {
    const directive = new IsAuthenticated();
    expect(directive).toBeTruthy();
  });
});
