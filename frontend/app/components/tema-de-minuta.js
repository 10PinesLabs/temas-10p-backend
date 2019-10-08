import Ember from 'ember';
import MinutaServiceInjected from "../mixins/minuta-service-injected";
import TemaDeMinutaServiceInjected from "../mixins/tema-de-minuta-service-injected";
import NavigatorInjected from "../mixins/navigator-injected";
import UserServiceInjected from "../mixins/user-service-injected";
import Tema from "../concepts/tema";

export default Ember.Component.extend(MinutaServiceInjected, TemaDeMinutaServiceInjected, NavigatorInjected, UserServiceInjected, {
  classNames: ['card'],

  mostrandoDetalle: false,
  textoExtendido: false,
  agregarItem: false,

  usuarios: Ember.computed('model.usuarios', function () {
    return this.get('model.usuarios');
  }),

  temaTratado: Ember.computed('model', function () {
    return this.get('temaDeMinuta').fueTratado;
  }),

  mostrarFlagActionItems: Ember.computed(function () {
    return (this.get('temaDeMinuta').fueTratado || !this.get('editable')) &&
      this.get('temaDeMinuta').actionItems && this.get('temaDeMinuta').actionItems.length;
  }),

  mostrarDescripcion: Ember.computed('mostrandoDetalle', function () {
    return (this.get('editable') && !this.get('temaTratado')) ||
      this.get('mostrandoDetalle');
  }),

  mostrarSwitchTratado: Ember.computed('mostrandoDetalle', function () {
    return this.get('editable') &&
      (!this.get('temaTratado') || this.get('mostrandoDetalle'));
  }),

  temaConComportamiento: Ember.computed('temaDeMinuta', function () {
    return Tema.create(this.get('temaDeMinuta.tema'));
  }),

  actions: {
    expandirDescripcion() {
      this._extenderTexto();
    },
    colapsarDescripcion() {
      this._colapsarTexto();
    },
    verDetalleDeTema() {
      this._mostrarDetalle();
    },
    ocultarDetalleDeTema() {
      this._colapsarTexto();
      this._ocultarDetalle();
    },
    ocultarAgregadoActionItem() {
      this.set('agregarItem', false);
    },
  },

  _extenderTexto() {
    this.set('textoExtendido', true);
  },
  _colapsarTexto() {
    this.set('textoExtendido', false);
  },
  _mostrarDetalle() {
    this.set('mostrandoDetalle', true);
  },
  _ocultarDetalle() {
    this.set('mostrandoDetalle', false);
  },
  _recargarLista() {
    this.get('router').refresh();
  },

});
