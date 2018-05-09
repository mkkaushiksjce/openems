import { Component, Injectable } from '@angular/core';
import { IonicPage, NavController, NavParams, ToastController } from 'ionic-angular';
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
import { ConfigImpl } from '../deviceconfig/config';
import { OverviewPage } from '../overview/overview';
import { LoginPage } from '../login/login';
import { Service } from '../shared/service/service';




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

  public edges: Edge[] = [];
  private socket: WebSocketSubject<any>;
 // public status: DefaultTypes.ConnectionStatus = "connecting";
 // public isWebsocketConnected: BehaviorSubject<boolean> = new BehaviorSubject(false);
 // private replyStreams: { [deviceName: string]: { [messageId: string]: Subject<any> } } = {};
  private platzhalter: { [messageId: string]: Subject<any> } = {};
 //public loginSubject = null;
  public isLoggedIn = new Subject<"loginDenied" | "loginSuccessful" | "loggedOut">();

  private event = new Subject<String>();
  constructor(
    private service: Service,
    
  ) {
  //  this.connect();
    
    // try to auto connect using token or session_id
    setTimeout(() => {
      this.connect();
    })
  }
  

  ionViewDidLoad() {
    console.log('ionViewDidLoad WebsocketPage');
  }




  public logIn(password: string): Subject<"loginDenied" | "loginSuccessful" | "loggedOut"> {
    //this.loginSubject = new Subject<Boolean>();
    this.send(DefaultMessages.authenticateLogin(password))
    console.log("Send")
    console.log(this.isLoggedIn);
    return this.isLoggedIn;
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

      // todo: Authenticate
      if ("authenticate" in message && "mode" in message.authenticate) {
        let mode = message.authenticate.mode;

        if (mode === "allow") {
          // authentication successful
        //  this.status = "online";
          this.isLoggedIn.next("loginSuccessful");

          if ("token" in message.authenticate){
            console.log("TOKEN IST DA")
            
            this.service.setToken(message.authenticate.token);
          }

        }
        else if (mode === "deny") {
        //  this.status = 'waiting for authentication';
          this.service.removeToken();
          this.isLoggedIn.next("loginDenied");
        }
        else if (mode === "loggedOut") {
          this.isLoggedIn.next("loggedOut");
          this.service.removeToken();
        }
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









  getConfig(edgeId: number) {
    console.log("getConfig()");
    /*
     * UI send
     */
    let message = {
      messageId: {
        ui: UUID.UUID()
      },
      edgeId: edgeId,
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

  // logout() {
  //   let message = {
  //     authenticate: {
  //       mode: "logout"
  //     }

  //   }
  //   console.log("SEND: ", message);
  //   // this.status = "connecting";
  //   this.socket.socket.send(JSON.stringify(message));
  // }

  logout() {
    this.send(DefaultMessages.authenticateLogout())
  }

  currentdatasub(edgeId: number, channels: DefaultTypes.ChannelAddresses): void {
    let message = {
      messageId: {
        ui: UUID.UUID()
      },
      edgeId: 0,
      currentData: {
        mode: "subscribe",
        channels: {
          _bridge0: ["State"],
          _bridge1: ["State"],
          _controller0: ["State"],
          _controller1: ["State"],
          _controller2: ["State"],
          _controller3: ["State"],
          _controller4: ["State"],
          _device0: ["State"],
          _device1: ["State"],
          _persistence0: ["State"],
          _scheduler0: ["State"],
          ess0: ["State"],
          meter0: ["State"],
          meter1: ["State"],
          system0: ["State"]
        }
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

