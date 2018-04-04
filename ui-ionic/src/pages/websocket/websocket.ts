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

/**
 * Generated class for the WebsocketPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@Injectable()
export class Websocket {

  public test = "Hallo Fabian";
  private socket: WebSocketSubject<any>;
  public status: DefaultTypes.ConnectionStatus = "connecting";
  public isWebsocketConnected: BehaviorSubject<boolean> = new BehaviorSubject(false);
  private replyStreams: { [deviceName: string]: { [messageId: string]: Subject<any> } } = {};

  private platzhalter: { [messageId: string]: Subject<any> } = {};
  // {
  //   "ID1": new Subject(),
  //   "ID2": new Subject()
  // }
  private event = new Subject<String>();
  constructor() {
    this.connect();
  }

  ionViewDidLoad() {
    console.log('ionViewDidLoad WebsocketPage');
  }

  testmethode() {
    console.log("Testmethode bestanden");
  }

  connect() {
    console.log("connect()");

    this.socket = WebSocketSubject.create("ws://" + location.hostname + ":8085");
    this.socket.subscribe(message => {
      /*
       * UI receive
       */
      console.log("RECV ", message);
      // {
      //   "messageId": {},
      //   "notification": {
      //     "type": "info",
      //     "message": "Authentication by token [] failed",
      //     "code": 103,
      //     "params": [
      //       ""
      //     ]
      //   }
      // }
      if ("messageId" in message && "ui" in message.messageId) {
        let messageId = message.messageId.ui;
        console.log("ID: ", messageId)
        this.platzhalter[messageId].next(message)
        console.log("danach 1")
      }
      console.log("danach 2")
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
}
