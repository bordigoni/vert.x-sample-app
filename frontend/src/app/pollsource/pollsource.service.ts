import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {PollSource} from './pollsource.model';

import 'rxjs/add/operator/map'

const baseUrl = "http://localhost:8080/client";

@Injectable()
export class PollsourceService {

  constructor(private http: Http) {
  }

  save(clientId: string, pollSource: PollSource): Observable<any> {
    return this.http.post(this.formatUrl(clientId), pollSource);
  }

  update(clientId: string, pollSource: PollSource): Observable<any> {
    return this.http.put(this.formatUrl(clientId, pollSource.id), pollSource);
  }

  getAll(clientId: string): Observable<Array<PollSource>> {
    return this.http.get(this.formatUrl(clientId))
      .map(r => r.json());
  }

  get(clientId: string, id: string): Observable<PollSource> {
    return this.http.get(this.formatUrl(clientId, id))
      .map(r => r.json());
  }

  remove(clientId: string, id: string) {
    this.http.delete(this.formatUrl(clientId, id));
  }


  private formatUrl(clientId: string, id?: string) {
    return baseUrl + '/' + clientId + '/pollsource' + (id ? '/' + id : '');
  }

}
