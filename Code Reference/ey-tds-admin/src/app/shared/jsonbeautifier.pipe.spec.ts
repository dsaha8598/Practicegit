import { JsonbeautifierPipe } from './jsonbeautifier.pipe';

describe('JsonbeautifierPipe', () => {
  it('create an instance', () => {
    const pipe = new JsonbeautifierPipe();
    expect(pipe).toBeTruthy();
  });
});
