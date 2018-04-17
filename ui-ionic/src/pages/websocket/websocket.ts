import { Component, Injectable } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { WebSocketSubject } from 'rxjs/observable/dom/WebSocketSubject';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { DefaultTypes } from '../shared/service/defaulttypes';
import { UUID } from 'angular2-uuid';
import { format } from 'date-fns';
import { Utils } from '../shared/service/utils';
import { Role } from '../type/role';
import { DefaultMessages } from '../shared/service/defaultmessages';
import { Device } from '../deviceconfig/device';

/**
 * Generated class for the WebsocketPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */


interface Edge {
  edgeId: number,
  name: string,
  comment: string,
  producttype: string,
  role: Role,
  online: boolean,
}

@Injectable()
export class Websocket {

  private edges: Edge[] = [];
  private socket: WebSocketSubject<any>;
  public status: DefaultTypes.ConnectionStatus = "connecting";
  public isWebsocketConnected: BehaviorSubject<boolean> = new BehaviorSubject(false);
  private replyStreams: { [deviceName: string]: { [messageId: string]: Subject<any> } } = {};
  private platzhalter: { [messageId: string]: Subject<any> } = {};

  private event = new Subject<String>();
  constructor() {
    this.connect();
  }

  ionViewDidLoad() {
    console.log('ionViewDidLoad WebsocketPage');
  }



  connect() {
    console.log("connect()");
    this.socket = WebSocketSubject.create("ws://" + location.hostname + ":8085");
    this.socket.subscribe(message => {
      /*
       * UI receive
       */
      console.log("RECV ", message);

      if ("messageId" in message && "ui" in message.messageId) {
        let messageId = message.messageId.ui;
        this.platzhalter[messageId].next(message)
      }

      /* 
      * Get Metadata
      */
      if ("metadata" in message) {
        if ("edges" in message.metadata) {
          this.edges = [];
          for (let edge of message.metadata.edges) {
            this.edges.push({
              edgeId: edge.id,
              name: edge.name,
              comment: edge.comment,
              producttype: edge.producttype,
              role: edge.role,
              online: edge.online
            })
          }
        }
      }
    });
  }

  login(): void {
    console.log("login()");
    let message = {
      authenticate: {
        mode: "login",
        password: "admin"
      }
    }
    console.log("SEND: ", message);
    this.socket.socket.send(JSON.stringify(message));
  }

  getConfig(edgeId: number) {
    console.log("getConfig()");
    /*
     * UI send
     */
    let message = {
      messageId: {
        ui: UUID.UUID()
      },
      edgeId: 0,
      config: {
        language: "de",
        mode: "query"
      }
    }
    let messageId = message.messageId.ui;
    this.platzhalter[messageId] = new Subject();
    console.log("Alle Platzhalter: ", this.platzhalter);

    this.platzhalter[messageId].subscribe(message => {
      console.log("Platzhalter: ", message);
      this.platzhalter[messageId].unsubscribe();
      delete this.platzhalter[messageId];
    })


    console.log("SEND: ", message);
    this.socket.socket.send(JSON.stringify(message));
  }

  queryhistoric(edgeId: number, fromDate: Date, toDate: Date, timezone: number, channels: DefaultTypes.ChannelAddresses): void {
    console.log("queryhistory");
    let message = {
      messageId: {
        ui: UUID.UUID()
      },
      edgeId: 0,
      historicData: {
        mode: "query",
        fromDate: format(fromDate, 'YYYY-MM-DD'),
        toDate: format(toDate, 'YYYY-MM-DD'),
        timezone: timezone,
        channels: channels
      }
    }
    let messageId = message.messageId.ui;
    this.platzhalter[messageId] = new Subject();
    console.log("Alle Platzhalter: ", this.platzhalter);

    this.platzhalter[messageId].subscribe(message => {
      console.log("Platzhalter: ", message);
      this.platzhalter[messageId].unsubscribe();
      delete this.platzhalter[messageId];
    })
    console.log("SEND: ", message);
    this.socket.socket.send(JSON.stringify(message));
  }

  logout() {
    let message = {
      authenticate: {
        mode: "logout"
      }

    }
    console.log("SEND: ", message);
    this.socket.socket.send(JSON.stringify(message));
  }

  public send(message: any): void {
    console.info("SEND: ", message);
    this.socket.socket.send(JSON.stringify(message));
  }

}

