import Ember from "ember";
import MinutaServiceInjected from "../../mixins/minuta-service-injected";
import NavigatorInjected from "../../mixins/navigator-injected";
import TemaDeMinutaServiceInjected from "../../mixins/tema-de-minuta-service-injected";
import UserServiceInjected from "../../mixins/user-service-injected";

export default Ember.Controller.extend(MinutaServiceInjected, TemaDeMinutaServiceInjected, NavigatorInjected, UserServiceInjected, {

  reunionId: Ember.computed('model.reunionId', function () {
    return this.get('model.reunionId');
  }),

  minuta:Ember.computed('model.minuta',function(){
    return this.get('model.minuta');
  }),

  fecha: Ember.computed('model.minuta', function(){
    return moment(this.get('model.minuta').fecha).format('DD-MM-YYYY');
  }),

  usuariosSeleccionables: Ember.computed('model.usuarios', 'usuariosSeleccionados', function () {
    var todosLosUsuarios = this.get('model.usuarios');
    var usuariosSeleccionados = this.get('usuariosSeleccionados');
    return todosLosUsuarios.filter(function (usuario) {
      return !usuariosSeleccionados.some(function(seleccionado){
        return usuario.id === seleccionado.id;
      });
    });
  }),

  usuariosSeleccionados: Ember.computed('model.votantes', function() {
    return this.get('model.votantes');
  }),

  temaAEditar:Ember.computed('temaSeleccionado', function(){
    let tema = this.get('temaSeleccionado');
    let actionItems=[];
    this.get('temaSeleccionado.actionItems').forEach((actionItem)=> actionItems.push(actionItem));
    return Ember.Object.extend().create({
      id: tema.id,
      idDeMinuta: tema.idDeMinuta,
      tema: tema.tema,
      conclusion: tema.conclusion,
      fueTratado: tema.fueTratado,
      actionItems:actionItems
    });
  }),

  actions: {

    verEditorDeConclusion(tema){
      this._mostrarEditorDeConclusion(tema);
    },

    cerrarEditor(){
      this._ocultarEditor();
    },

    guardarConclusion(fueTratado){
      var tema=this.get('temaAEditar');
      tema.actionItems.forEach((actionItem)=>{
        delete actionItem.usuarios;
        delete actionItem.usuariosSeleccionables;
      });

      tema.set('fueTratado', fueTratado);

      this.temaDeMinutaService().updateTemaDeMinuta(tema)
        .then(()=> {
          this._recargarLista();

          this._ocultarEditor();
        });
    },

    quitarAsistente(usuario){
      this.agregarUsuarioAYSacarUsuarioDe(usuario, 'usuariosSeleccionables', 'usuariosSeleccionados');

      this.guardarUsuariosSeleccionados();
    },

    agregarAsistente(usuario){
      this.agregarUsuarioAYSacarUsuarioDe(usuario, 'usuariosSeleccionados', 'usuariosSeleccionables');

      this.guardarUsuariosSeleccionados();
      },
  },

  anchoDeTabla: 's12',

  temaSeleccionado: Ember.computed('minuta', 'indiceSeleccionado', function () {
    var indiceSeleccionado = this.get('indiceSeleccionado');
    var temas = this.get('minuta.temas');

    return temas.objectAt(indiceSeleccionado);
  }),

  _mostrarEditorDeConclusion(tema){
    var indiceClickeado = this.get('minuta.temas').indexOf(tema);
    this.set('indiceSeleccionado', indiceClickeado);
    this._mostrarEditor();
  },

  _mostrarEditor(){
    this.set('anchoDeTabla', 's4');
    this.set('mostrandoEditor', true);
  },

  _ocultarEditor(){
    this.set('indiceSeleccionado',null);
    this.set('mostrandoEditor', false);
    this.set('anchoDeTabla', 's12');
  },

  _recargarLista(){
    this.get('target.router').refresh();
  },

  guardarUsuariosSeleccionados(){
    this.set('model.minuta.asistentes', this.get('usuariosSeleccionados'));
    this.minutaService().updateMinuta(this.get('model.minuta'));
  },

  agregarUsuarioAYSacarUsuarioDe(usuario, nombreLista1, nombreLista2){
    let lista1 = this.get(nombreLista1);
    lista1.pushObject(usuario);
    this.set(nombreLista1, lista1);

    let lista2 = this.get(nombreLista2);
    lista2.removeObject(usuario);
    this.set(nombreLista2, lista2);
  },
});
