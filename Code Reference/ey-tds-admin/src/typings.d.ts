/*
 * Extra typings definitions
 */

// declare module 'js-string-compression';

// Allow .json files imports
declare module '*.json';

// SystemJS module definition
declare var module: NodeModule;
interface NodeModule {
  id: string;
}
