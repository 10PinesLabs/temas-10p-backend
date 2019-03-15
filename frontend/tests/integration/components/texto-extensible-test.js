/* jshint expr:true */
import { expect } from 'chai';
import {
  describeComponent,
  it
} from 'ember-mocha';
import hbs from 'htmlbars-inline-precompile';

describeComponent(
  'texto-extensible',
  'Integration: TextoExtensibleComponent',
  {
    integration: true
  },
  function() {
    it('renders', function() {
      // Set any properties with this.set('myProperty', 'value');
      // Handle any actions with this.on('myAction', function(val) { ... });
      // Template block usage:
      // this.render(hbs`
      //   {{#texto-extensible}}
      //     template content
      //   {{/texto-extensible}}
      // `);

      this.render(hbs`{{texto-extensible}}`);
      expect(this.$()).to.have.length(1);
    });
  }
);